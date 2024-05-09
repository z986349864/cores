package com.zd.core.mq.failretry.entity;

public interface IRetryResultProcessStrategy {

    /**
     * 重试成功后的处理策略
     *
     * @param failRetryEntity
     */
    void onSuccess(FailRetryEntity failRetryEntity);

    /**
     * 重试失败到达最大次数时的处理策略
     *
     * @param failRetryEntity
     */
    void onFail(FailRetryEntity failRetryEntity);

}
