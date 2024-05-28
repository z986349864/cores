package com.zd.core.excel.entity;

import java.util.Objects;

public class CellError extends CellValue {

    private static final long serialVersionUID = -8170590974581160001L;

    private Integer errorType;

    public Integer getErrorType() {
        return errorType;
    }

    public void setErrorType(Integer errorType) {
        this.errorType = errorType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        CellError cellError = (CellError) o;
        return Objects.equals(errorType, cellError.errorType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), errorType);
    }
}