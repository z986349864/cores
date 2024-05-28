package com.zd.core.utils.tool;

import com.jcraft.jsch.*;
import com.zd.core.utils.SftpLock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;

public class SftpClient implements SftpAndFtpClient {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 服务器地址
     */
    private String host;

    /**
     * 服务器端口
     */
    private int port;

    /**
     * 用户登录名
     */
    private String username;

    /**
     * 用户登录密码
     */
    private String password;

    /**
     * sftp连接通道对象
     */
    private Channel channel = null;

    /**
     * session对象
     */
    private Session sshSession = null;

    /**
     * sftp操作通道对象
     */
    private ChannelSftp sftp = null;

    /**
     * lock
     */
    private SftpLock sftpLock = null;

    private final String lockPrefix=this.getClass().getName();

    public SftpClient(String host, int port, String username, String password) {
        this(host, port, username, password, null);
    }

    public SftpClient(String host, int port, String username, String password, SftpLock sftpLock) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sftpLock = sftpLock;
    }

    /**
     * 连接sftp服务器
     *
     * @return
     */
    @Override
    public void connect() {
        try {
            JSch jsch = new JSch();
            jsch.getSession(username, host, port);
            sshSession = jsch.getSession(username, host, port);
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            logger.error("connect",e);
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
        if (StringUtils.isNotBlank(directory) && StringUtils.isNotBlank(fileName)) {
            try {
                sftp.cd(directory);
                sftp.lstat(fileName);
            } catch (SftpException e) {
                if (e.id == SSH_FX_NO_SUCH_FILE) {
                    return false;
                } else {
                    throw new RuntimeException(e);
                }
            }
            return true;
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
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
//            logger.error("sftp服务器没有这个目录,{}", e);
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    /**
     * 创建目录
     *
     * @param createPath
     * @author DINGYONG
     */
    @Override
    public void createDir(String createPath) {
        if (sftpLock == null) {
            createFileDir(createPath);//并发情况下会抛出 "sftp服务器创建目录失败" exception
        }
        else
        {
            try {
                if (sftpLock.tryLock(lockPrefix.concat(createPath), TimeUnit.SECONDS, 3, -1)) {
                    createFileDir(createPath);
                    sftpLock.unlock(lockPrefix.concat(createPath));
                }
            }
            catch (Exception exception) {
                sftpLock.unlock(lockPrefix.concat(createPath));
            }
        }
    }

    private void createFileDir(String createPath) {
        try {
            if (isDirExist(createPath)) {
                sftp.cd(createPath);
            }
            String[] pathArray = createPath.split("/");
            StringBuilder filePath = new StringBuilder("/");
            for (String path : pathArray) {
                if ("".equals(path)) {
                    continue;
                }
                filePath.append(path).append("/");
                if (isDirExist(filePath.toString())) {
                    sftp.cd(filePath.toString());
                } else {
                    // 建立目录
                    sftp.mkdir(filePath.toString());
                    // 进入并设置为当前目录
                    sftp.cd(filePath.toString());
                }
            }
            sftp.cd(createPath);
        } catch (Exception e) {
            logger.error("sftp服务器创建目录失败,{}", e);
        }
    }


    @Override
    public List<String> listFiles(String directory) {
        List<String> fileNames = new ArrayList<>();
        try {
            List<ChannelSftp.LsEntry> lsEntries =  sftp.ls(directory);
            for (ChannelSftp.LsEntry lsEntry : lsEntries) {
                if(!lsEntry.getAttrs().isDir()){
                    fileNames.add(lsEntry.getFilename());
                }
            }
        } catch (SftpException e) {
            logger.error("ftp listFiles" ,e);
            throw new RuntimeException(e);
        }
        return fileNames;
    }

    /**
     * 上传文件
     *
     * @param directory  上传的目录
     * @param uploadFile 要上传的文件
     */
    @Override
    public void upload(String directory, File uploadFile, String fileName) {
        try {
            createDir(directory);
            sftp.put(new FileInputStream(uploadFile), fileName);
        } catch (Exception e) {
            logger.error("上传文件到sftp服务器异常,{}", e);
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
            sftp.put(inputStream, fileName);
        } catch (Exception e) {
            logger.error("上传文件到sftp服务器异常,{}", e);
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
            sftp.cd(directory);
            File file = new File(saveFile);
            sftp.get(downloadFile, new FileOutputStream(file));
        } catch (Exception e) {
            logger.error("从sftp服务器下载文件异常,{}", e);
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
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            sftp.cd(directory);
            sftp.get(downloadFile, outputStream);
            //注意返回接收的inputStream需要关闭
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (SftpException e) {
            logger.error("sftp downloadForStream" ,e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                logger.error("读取sftp文件内容异常,{}", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 读取指定文件,返回字节
     *
     * @param directory
     * @param downloadFile
     * @return byte
     * @throws SftpException
     * @throws IOException
     * @author DINGYONG
     */
    @Override
    public byte[] downloadForBytes(String directory, String downloadFile) {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            sftp.cd(directory);
            sftp.get(downloadFile, outputStream);
            return outputStream.toByteArray();
        } catch (SftpException e) {
            logger.error("downloadForBytes ",e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                logger.error("读取sftp文件内容异常,{}", e);
            }
        }
    }

    /**
     * 重命名文件
     *
     * @param oldPath
     * @param newPath
     * @throws SftpException
     * @throws IOException
     * @author DINGYONG
     */
    @Override
    public void renameFileName(String oldPath, String newPath) {
        try {
            sftp.rename(oldPath, newPath);
        } catch (SftpException e) {
            logger.error("renameFileName ",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    @Override
    public void delete(String directory, String deleteFile) {
        try {
            sftp.cd(directory);
            sftp.rm(deleteFile);
        } catch (Exception e) {
            logger.error("删除sftp文件发生异常,{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断是否断开了sftp连接通道
     *
     * @return true
     * @author DINGYONG
     */
    @Override
    public boolean isClosed() {
        return sshSession == null || !sshSession.isConnected() || channel == null || !channel.isConnected();
    }


    /**
     * 关闭资源
     *
     * @author DINGYONG
     */
    @Override
    public void close() {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (sshSession != null && sshSession.isConnected()) {
            sshSession.disconnect();
        }
    }
}
