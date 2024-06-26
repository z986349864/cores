package com.zd.core.mq.consumer;

import java.util.Date;

public interface IConsumerRetryService {

    /**
     * 消费失败重试
     *
     * @param lastSendTime
     * @param thisTime
     * @param businessType
     * @return void
     */
    void retry(String lastSendTime, String thisTime, Integer businessType);
}
