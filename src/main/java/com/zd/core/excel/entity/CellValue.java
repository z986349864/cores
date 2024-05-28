package com.zd.core.excel.entity;

import java.io.Serializable;
import java.util.Objects;

public class CellValue implements Serializable {

    private static final long serialVersionUID = 3136987753533793416L;

    private Integer columnNo;

    private String columnName;

    private String value;

    public Integer getColumnNo() {
        return columnNo;
    }

    public void setColumnNo(Integer columnNo) {
        this.columnNo = columnNo;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CellValue cellBase = (CellValue) o;
        return Objects.equals(columnNo, cellBase.columnNo) &&
                Objects.equals(columnName, cellBase.columnName) &&
                Objects.equals(value, cellBase.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnNo, columnName, value);
    }
}
