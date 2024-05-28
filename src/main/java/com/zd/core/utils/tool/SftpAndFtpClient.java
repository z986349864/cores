package com.zd.core.utils.tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface SftpAndFtpClient extends AutoCloseable {
    /**
     * 连接sftp服务器
     *
     * @return
     */
    void connect();

    /**
     * 判断文件是否存在
     *
     * @param directory
     * @param fileName
     * @return
     */
    boolean isFileExist(String directory, String fileName);

    /**
     * 判断目录是否存在
     *
     * @param directory 上传的目录
     */
    boolean isDirExist(String directory);

    /**
     * 创建目录
     *
     * @param createPath
     * @author DINGYONG
     */
    void createDir(String createPath);

    /**
     * 列出目录下的文件
     *
     * @param directory 要列出的目录
     * @return
     * @throws
     */
    List listFiles(String directory);

    /**
     * 上传文件
     *
     * @param directory  上传的目录
     * @param uploadFile 要上传的文件
     */
    void upload(String directory, File uploadFile, String fileName) ;

    /**
     * 上传文件流
     *
     * @param directory   上传的目录
     * @param inputStream 要上传的文件
     */
    void uploadByInputStream(String directory, InputStream inputStream, String fileName);

    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件
     * @param saveFile     存在本地的路径
     */
    void download(String directory, String downloadFile, String saveFile) ;

    /**
     * 读取指定文件,返回输入流
     *
     * @param directory
     * @param downloadFile
     * @return
     * @author DINGYONG
     */
    InputStream downloadForStream(String directory, String downloadFile);

    /**
     * 读取指定文件,返回字节
     *
     * @param directory
     * @param downloadFile
     * @return byte
     * @throws IOException
     * @author DINGYONG
     */
    byte[] downloadForBytes(String directory, String downloadFile);

    /**
     * 重命名文件
     *
     * @param oldPath
     * @param newPath
     * @throws IOException
     * @author DINGYONG
     */
    void renameFileName(String oldPath, String newPath);

    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    void delete(String directory, String deleteFile);

    /**
     * 判断是否断开了sftp连接通道
     *
     * @return true
     * @author DINGYONG
     */
    boolean isClosed();
}
