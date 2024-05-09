package com.zd.core.mq.failretry.strategy;

import com.zd.core.mq.failretry.entity.FailRetryEntity;
import com.zd.core.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DefaultNextRetryTimeStrategy implements INextRetryTimeStrategy {
    /**
     * 计算下次重试时间
     *
     * @param failRetryEntity
     * @return
     */
    @Override
    public Date calculateNextRetryTime(FailRetryEntity failRetryEntity) {
        if (failRetryEntity == null
                || failRetryEntity.getRetryTimes() == null
                || failRetryEntity.getRetryTimes() == 0
                || failRetryEntity.getRetryTime() == null) {
            //第一次，默认5分钟
            return DateUtils.addSecondToDate(new Date(), 60);
        }
        return DateUtils.addSecondToDate(new Date(), (long) ((Math.pow(2, failRetryEntity.getRetryTimes())) * 60));
    }
}

