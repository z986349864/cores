package com.zd.core.mq.producer;

public interface IMessageProducer<T> {

    /**
     * 发送消息
     *
     * @param message
     * @return void
     */
    void sendMessage(Message<T> message);

    /**
     * 事务提交后发送消息
     *
     * @param message
     * @return void
     */
    void sendMessageAfterTransactionCommitted(Message<T> message);

}
