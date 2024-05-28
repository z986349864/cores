package com.zd.core.excel.vo;

import java.io.Serializable;

public class CoreImportResultVO implements Serializable {
    private static final long serialVersionUID = 3514614804985103011L;
    // total count
    private int totalCount;
    // success count
    private int successCount;
    // failed count
    private int failedCount;
    // result file url
    private String resultUrl;

    public CoreImportResultVO() {
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getSuccessCount() {
        return this.successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return this.failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public String getResultUrl() {
        return this.resultUrl;
    }

    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }
}
