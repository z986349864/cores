package com.zd.core.mq.failretry.dto;

import com.zd.core.mq.failretry.entity.request.PagableVO;

import java.io.Serializable;

public class FailRetryDto extends PagableVO implements Serializable {
    /**
     * 重试开始时间
     */
    private Long startTime;

    /**
     * 重试结束时间
     */
    private Long endTime;
    /**
     * 业务类型
     */
    private Integer businessType;
    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 重试状态：0重试失败，1重试成功
     */
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

}