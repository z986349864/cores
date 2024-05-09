package com.zd.core.mq.consumer;

import com.zd.core.mq.failretry.entity.IRetryResultProcessStrategy;
import com.zd.core.mq.failretry.strategy.DefaultNextRetryTimeStrategy;
import com.zd.core.mq.failretry.strategy.INextRetryTimeStrategy;

public interface IConsumer<T> {

    /**
     * 消费数据
     * 只要处理业务 异常直接抛出
     *
     * @param data
     * @return void
     */
    void consumer(T data);

    /**
     * 自定义消费失败重试后的处理策略 默认不提供
     * 默认不提供
     *
     * @param
     * @return com.itiaoling.vos.core.failretry.strategy.IRetryResultProcessStrategy
     */
    default IRetryResultProcessStrategy registeredRetryResultProcessStrategy() {
        return null;
    }

    /**
     * 下次重试时间的实现策略
     * @param
     * @return com.itiaoling.vos.core.failretry.strategy.INextRetryTimeStrategy
     */
    default INextRetryTimeStrategy getNextRetryTimeStrategy() {
        return new DefaultNextRetryTimeStrategy();
    }

    /**
     * 重试数量 默认返回5次
     * @return
     */
    default Integer getRetriesNumber(){
        return 5;
    }

}
