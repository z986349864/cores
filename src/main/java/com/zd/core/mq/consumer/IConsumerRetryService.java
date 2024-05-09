package com.zd.core.mq.consumer;

public interface IConsumerRetryService {

    /**
     * 消费失败重试
     *
     * @param lastSendTime
     * @param thisTime
     * @param businessType
     * @return void
     */
    void retry(Long lastSendTime, Long thisTime, Integer businessType);
}
