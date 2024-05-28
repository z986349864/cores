package com.zd.core.excel.constrant;

public class ExcelConstants {
    /**
     * 设定导入文件最大2M
     */
    public static final int MAX_IMPORT_UPLOAD = 2048000;
    /**
     * 设定最大导出量
     */
    public static final int MIN_EXPORT_SIZE = 1;
    public static final int MAX_EXPORT_SIZE = 3000;
    public static final int DATE_FORMAT_TYPE_SIMPLE = 0;
    public static final int DATE_FORMAT_TYPE_ALL = 1;
    public static final String FILE_EXTENSION_XLSX = ".xlsx";

    /**
     * excel导出失败类型
     */
    public static final String EXCEL_EXPORT_FAIL_REASON_PARAM_ERROR = "0";
    public static final String EXCEL_EXPORT_FAIL_REASON_TITLE_EMPTY = "1";
    public static final String EXCEL_EXPORT_FAIL_REASON_UNEXPECTED = "99";

}
