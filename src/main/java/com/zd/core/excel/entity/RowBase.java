package com.zd.core.excel.entity;

import java.io.Serializable;
import java.util.List;

public class RowBase implements Serializable {

    private static final long serialVersionUID = 5738418612577706138L;

    private String analyseLanguage;

    private Integer lineNumber;

    private List<CellError> errors;

    public String getAnalyseLanguage() {
        return analyseLanguage;
    }

    public void setAnalyseLanguage(String analyseLanguage) {
        this.analyseLanguage = analyseLanguage;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<CellError> getErrors() {
        return errors;
    }

    public void setErrors(List<CellError> errors) {
        this.errors = errors;
    }
}
