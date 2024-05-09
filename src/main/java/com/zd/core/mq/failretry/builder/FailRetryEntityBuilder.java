package com.zd.core.mq.failretry.builder;

import com.zd.core.constant.ApplicationConstants;
import com.zd.core.exception.BusinessException;
import com.zd.core.mq.failretry.entity.FailRetryEntity;
import com.zd.core.mq.failretry.strategy.INextRetryTimeStrategy;
import com.zd.core.utils.TransformUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Objects;

public class FailRetryEntityBuilder {

    /**
     * 重试业务类型
     */
    private Integer businessType;

    /**
     * 业务主键
     */
    private String businessId;

    /**
     * 重试需要使用的参数，json格式
     */
    private String params;

    /**
     * 重试状态 0/null 需要重试，1不需要重试
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 重试次数
     */
    private Integer retryTimes;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 下次重试时间
     */
    private Date retryTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 下次重试时间计算策略
     */
    private INextRetryTimeStrategy retryTimeStrategy;

    public FailRetryEntity build() {
        if (Objects.isNull(businessType)) {
            throw new BusinessException("Fail Retry Param Error, businessType is null.");
        }
        if (StringUtils.isBlank(businessId)) {
            throw new BusinessException("Fail Retry Param Error, businessId is blank.");
        }
        if (Objects.isNull(status)) {
            throw new BusinessException("Fail Retry Param Error, status is null.");
        }
        if (Objects.equals(status, ApplicationConstants.FAIL)) {
            if (Objects.isNull(maxRetryTimes)) {
                throw new BusinessException("Fail Retry Param Error, maxRetryTimes is null.");
            }
            if (StringUtils.isBlank(failReason)) {
                throw new BusinessException("Fail Retry Param Error, failReason is blank.");
            }
        }
        return TransformUtils.copy2(this, FailRetryEntity.class);
    }

    public FailRetryEntityBuilder success() {
        return setStatus(ApplicationConstants.SUCCESS);
    }

    public FailRetryEntityBuilder fail() {
        return setStatus(ApplicationConstants.FAIL);
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public FailRetryEntityBuilder setBusinessType(Integer businessType) {
        this.businessType = businessType;
        return this;
    }

    public String getBusinessId() {
        return businessId;
    }

    public FailRetryEntityBuilder setBusinessId(String businessId) {
        this.businessId = businessId;
        return this;
    }

    public String getParams() {
        return params;
    }

    public FailRetryEntityBuilder setParams(String params) {
        this.params = params;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public FailRetryEntityBuilder setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getFailReason() {
        return failReason;
    }

    public FailRetryEntityBuilder setFailReason(String failReason) {
        this.failReason = failReason;
        return this;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public FailRetryEntityBuilder setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public Integer getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public FailRetryEntityBuilder setMaxRetryTimes(Integer maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
        return this;
    }

    public Date getRetryTime() {
        return retryTime;
    }

    public FailRetryEntityBuilder setRetryTime(Date retryTime) {
        this.retryTime = retryTime;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public FailRetryEntityBuilder setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public INextRetryTimeStrategy getRetryTimeStrategy() {
        return retryTimeStrategy;
    }

    public FailRetryEntityBuilder setRetryTimeStrategy(INextRetryTimeStrategy retryTimeStrategy) {
        this.retryTimeStrategy = retryTimeStrategy;
        return this;
    }
}