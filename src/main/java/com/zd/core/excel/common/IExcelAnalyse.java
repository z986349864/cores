package com.zd.core.excel.common;

import com.zd.core.excel.entity.RowBase;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface IExcelAnalyse {
    /**
     * excel解析单个sheet页,指定sheet页的位置和起始行
     * @param multipartFile
     * @param sheetIdx
     * @param startRowNum
     * @param maxSize
     * @param targetClass
     * @param rowValue
     * @param applicationContext
     * @param <T>
     * @return
     */
    <T extends RowBase> List<T> analyseSingle(MultipartFile multipartFile, int sheetIdx, int startRowNum, long maxSize, @NotNull Class<T> targetClass, boolean rowValue, ApplicationContext applicationContext);
    /**
     * excel解析单个sheet页,固定第一个sheet页，从第一行开始
     * @param multipartFile
     * @param maxSize
     * @param targetClass
     * @param applicationContext
     * @param <T>
     * @return
     */
    <T extends RowBase> List<T> analyseSingle(MultipartFile multipartFile, long maxSize, @NotNull Class<T> targetClass , ApplicationContext applicationContext);
}
