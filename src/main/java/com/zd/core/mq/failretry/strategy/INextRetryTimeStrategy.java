package com.zd.core.mq.failretry.strategy;

import com.zd.core.mq.failretry.entity.FailRetryEntity;

import java.util.Date;

public interface INextRetryTimeStrategy {

    /**
     * 计算下次重试时间
     * @param failRetryEntity
     */
    Date calculateNextRetryTime(FailRetryEntity failRetryEntity);
}
