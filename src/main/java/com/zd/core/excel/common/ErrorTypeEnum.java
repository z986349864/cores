package com.zd.core.excel.common;

import java.util.Objects;

public enum ErrorTypeEnum {

    /**
     * Excel 异常类型：1.不能为空、2.无效值、3.与文件中的其他数据重复、4.与数据库已有数据重复
     */
    UNKNOWN(-1, "UNKNOWN"),
    EMPTY(1, "empty"),
    INVALID_VALUE(2, "invalid value"),
    DUPLICATE_IN_FILE(3, "duplicate in file"),
    DUPLICATE_IN_DATABASE(4, "duplicate in database"),
    LENGTH_ERROR(5, "length error"),
    RANGE_ERROR(6, "range error");

    public final int code;
    public final String value;

    ErrorTypeEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public boolean check(Number code) {
        return Objects.nonNull(code) && Objects.equals(code.intValue(), this.code);
    }
}
