package com.zd.core.mq.failretry.entity;

import com.zd.core.mq.failretry.builder.FailRetryEntityBuilder;
import com.zd.core.mq.failretry.strategy.INextRetryTimeStrategy;

import java.util.Date;

public class FailRetryEntity {

    /**
     * 主键
     */
    private String id;

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
     * 是否有效 0：无效，1有效
     */
    private Integer active;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private String createUserCode;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 修改人
     */
    private String modifyUserCode;

    /**
     * 下次重试时间计算策略
     */
    private INextRetryTimeStrategy retryTimeStrategy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Integer getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(Integer maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public Date getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(Date retryTime) {
        this.retryTime = retryTime;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateUserCode() {
        return createUserCode;
    }

    public void setCreateUserCode(String createUserCode) {
        this.createUserCode = createUserCode;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getModifyUserCode() {
        return modifyUserCode;
    }

    public void setModifyUserCode(String modifyUserCode) {
        this.modifyUserCode = modifyUserCode;
    }

    public INextRetryTimeStrategy getRetryTimeStrategy() {
        return retryTimeStrategy;
    }

    public void setRetryTimeStrategy(INextRetryTimeStrategy retryTimeStrategy) {
        this.retryTimeStrategy = retryTimeStrategy;
    }

    public static FailRetryEntityBuilder builder(Integer businessType) {
        return new FailRetryEntityBuilder()
                .setBusinessType(businessType);
    }

    public static FailRetryEntityBuilder builder(Integer businessType, String businessId) {
        return new FailRetryEntityBuilder()
                .setBusinessType(businessType)
                .setBusinessId(businessId);
    }
}