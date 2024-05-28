package com.zd.core.utils.tool;

import com.zd.core.utils.SftpLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FtpClient implements SftpAndFtpClient {

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 超时时间
     */
    private static final int DEFAULT_TIMEOUT = 60 * 1000;

    /**
     * 主机名或者ip地址
     */
    private String host;

    /**
     * ftp服务器端口
     */
    private int port;

    /**
     * ftp用户名
     */
    private String username;

    /**
     * ftp密码
     */
    private String password;

    /**
     * ftpClient对象
     */
    private FTPClient ftpClient;

    /**
     * lock
     */
    private SftpLock ftpLock = null;

    private final String lockPrefix=this.getClass().getName();

    public FtpClient(String host, int port, String username, String password, SftpLock ftpLock) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.ftpLock = ftpLock;
    }

    public FtpClient(String host, int port, String username, String password) {
        this(host, port, username, password, null);
    }

    /**
     * 连接sftp服务器
     *
     * @return
     */
    @Override
    public void connect() {
        ftpClient = null;

        try {
            //创建一个ftp客户端
            ftpClient = new FTPClient();
            // 连接FTP服务器
            ftpClient.connect(this.host, this.port);
            // 登陆FTP服务器
            ftpClient.login(this.username, this.password);

            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                logger.info("未连接到FTP，用户名或密码错误。");
                ftpClient.disconnect();
            } else {
                logger.info("FTP连接成功。");
            }
        } catch (SocketException e) {
            logger.error("FTP的IP地址可能错误，请正确配置。",e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("FTP的端口错误,请正确配置。",e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 判断文件是否存在
     *
     * @param directory
     * @param fileName
     * @return
     */
    @Override
    public boolean isFileExist(String directory, String fileName) {
        try {
            if (StringUtils.isNotBlank(directory) && StringUtils.isNotBlank(fileName)) {
                ftpClient.enterLocalActiveMode();
                // 设置文件类型为二进制，与ASCII有区别
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                // 设置编码格式
                ftpClient.setControlEncoding("GBK");
                // 进入文件所在目录，注意编码格式，以能够正确识别中文目录
                ftpClient.changeWorkingDirectory(new String(directory.getBytes("GBK"), FTP.DEFAULT_CONTROL_ENCODING));
                // 检验文件是否存在
                InputStream is = ftpClient.retrieveFileStream(new String(fileName.getBytes("GBK"), FTP.DEFAULT_CONTROL_ENCODING));
                if (is == null || ftpClient.getReplyCode() == FTPReply.FILE_UNAVAILABLE) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("isFileExist：",e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("isFileExist：",e);
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * 判断目录是否存在
     *
     * @param directory 上传的目录
     */
    @Override
    public boolean isDirExist(String directory) {
        try {
            if (StringUtils.isNotBlank(directory)) {
                ftpClient.enterLocalActiveMode();
                // 设置文件类型为二进制，与ASCII有区别
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                // 设置编码格式
                ftpClient.setControlEncoding("GBK");
                // 进入文件所在目录，注意编码格式，以能够正确识别中文目录
                return ftpClient.changeWorkingDirectory(new String(directory.getBytes("GBK"), FTP.DEFAULT_CONTROL_ENCODING));
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("isDirExist：",e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("isDirExist：",e);
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * 创建目录
     *
     * @param createPath
     * @author DINGYONG
     */
    @Override
    public void createDir(String createPath) {
        if (ftpLock == null) {
            createFileDir(createPath);
        } else {
            try {
                if (ftpLock.tryLock(lockPrefix.concat(createPath), TimeUnit.SECONDS, 3, -1)) {
                    createFileDir(createPath);
                    ftpLock.unlock(lockPrefix.concat(createPath));
                }
            }
            catch (Exception exception) {
                ftpLock.unlock(lockPrefix.concat(createPath));
            }
        }
    }

    private void createFileDir(String createPath) {
        try {
            ftpClient.makeDirectory(createPath);
            ftpClient.changeWorkingDirectory(createPath);
        } catch (IOException e) {
            logger.error("ftp服务器创建目录失败,{}", e);
            throw new RuntimeException();
        }
    }

    /**
     * 列出目录下的文件
     *
     * @param filePath 要列出的目录
     * @return
     * @throws
     */
    @Override
    public List<String> listFiles(String filePath) {
        FTPFile[] ftpFiles = new FTPFile[0];
        List<String> fileList = new ArrayList<>();
        try {
            ftpFiles = ftpClient.listFiles(filePath);
            if (ftpFiles != null) {
                for (int i = 0; i < ftpFiles.length; i++) {
                    FTPFile ftpFile = ftpFiles[i];
                    if (ftpFile.isFile()) {
                        fileList.add(ftpFile.getName());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("ftp listFiles" ,e);
            throw new RuntimeException(e);
        }
        return fileList;
    }

    /**
     * 上传文件
     *
     * @param directory  上传的目录
     * @param uploadFile 要上传的文件
     */
    @Override
    public void upload(String directory, File uploadFile, String fileName) {
        if (!uploadFile.exists()) {
            logger.error("Can't upload '" + uploadFile.getAbsolutePath() + "'. This file doesn't exist.");
            throw new RuntimeException();
        }

        try (InputStream in = new BufferedInputStream(new FileInputStream(uploadFile)) ) {
            if (!ftpClient.storeFile(fileName, in)) {
                throw new IOException("Can't upload file '" + fileName + "' to FTP server. Check FTP permissions and path.");
            }
        } catch (FileNotFoundException e) {
            logger.error("ftp upload" ,e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("ftp upload" ,e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件流
     *
     * @param directory   上传的目录
     * @param inputStream 要上传的文件
     */
    @Override
    public void uploadByInputStream(String directory, InputStream inputStream, String fileName) {
        try {
            createDir(directory);
            ftpClient.appendFile(fileName, inputStream);
        } catch (Exception e) {
            logger.error("上传文件到ftp服务器异常,{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件
     * @param saveFile     存在本地的路径
     */
    @Override
    public void download(String directory, String downloadFile, String saveFile) {
        try {
            ftpClient.changeWorkingDirectory(directory);//转移到FTP服务器目录
            FTPFile[] fs = ftpClient.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(downloadFile)) {
                    File localFile = new File(saveFile + "/" + ff.getName());

                    OutputStream is = new FileOutputStream(localFile);
                    ftpClient.retrieveFile(ff.getName(), is);
                    is.close();
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("ftp download" ,e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("ftp download" ,e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取指定文件,返回输入流
     *
     * @param directory
     * @param downloadFile
     * @return
     * @author DINGYONG
     */
    @Override
    public InputStream downloadForStream(String directory, String downloadFile) {
        StringBuffer resultBuffer = new StringBuffer();
        logger.info("开始读取绝对路径" + directory + "文件!");
        try {
            ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(directory);
            return ftpClient.retrieveFileStream(directory + downloadFile);
        } catch (FileNotFoundException e) {
            logger.error("没有找到" + directory + "文件",e);
            throw new RuntimeException(e);
        } catch (SocketException e) {
            logger.error("连接FTP失败.",e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("文件读取错误。",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取指定文件,返回字节
     *
     * @param directory
     * @param downloadFile
     * @return byte
     * @throws IOException
     * @author DINGYONG
     */
    @Override
    public byte[] downloadForBytes(String directory, String downloadFile) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            InputStream in = downloadForStream(directory,downloadFile);
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }catch (IOException e) {
            logger.error("downloadForBytes：",e);
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }

    /**
     * 重命名文件
     *
     * @param oldPath
     * @param newPath
     * @throws IOException
     * @author DINGYONG
     */
    @Override
    public void renameFileName(String oldPath, String newPath) {
        if (ftpClient != null) {
            try {
                ftpClient.rename(oldPath, newPath);
            } catch (IOException e) {
                logger.error("renameFileName：",e);
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void delete(String directory, String deleteFile) {
        try {
            ftpClient.changeWorkingDirectory(directory);
            if (!ftpClient.deleteFile(deleteFile)) {
                throw new IOException("Can't remove file '" + deleteFile + "' from FTP server.");
            }
        } catch (IOException e) {
            logger.error("ftp delete" ,e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isClosed() {
        return !isConnected() || !isLogin();
    }

    @Override
    public void close() {
        try {
            if (ftpClient != null) {
                ftpClient.logout();
            }
        } catch (Exception e) {
            logger.error("ftp close:",e);
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                    logger.error("ftp close：",ioe);
                    throw new RuntimeException(ioe);
                }
            }
        }
    }

    private boolean isLogin() {
        if (ftpClient != null) {
            try {
                return ftpClient.sendNoOp();
            } catch (IOException e) {
                logger.error("ftp islogin" ,e);
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isConnected() {
        if (ftpClient != null) {
            return ftpClient.isConnected();
        }
        return false;
    }
}