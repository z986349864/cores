package com.zd.core.mq.producer;

import com.alibaba.fastjson.JSON;
import com.zd.core.exception.BusinessException;
import com.zd.core.mq.datasource.transacion.TransactionAfterCommitExecutor;
import com.zd.core.mq.failretry.builder.FailRetryEntityBuilder;
import com.zd.core.mq.failretry.entity.FailRetryEntity;
import com.zd.core.mq.failretry.service.IFailRetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class RabbitMQMessageProducer<T> implements IMessageProducer<T> {

    private Logger logger = LoggerFactory.getLogger(RabbitMQMessageProducer.class);
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private IFailRetryService failRetryService;
    @Autowired(required = false)
    private TransactionAfterCommitExecutor afterCommitExecutor;
    @Autowired
    private MessagePostProcessor correlationIdProcessor;

    /**
     * 发送消息
     *
     * @param message
     * @return void
     */
    @Override
    public void sendMessage(Message<T> message) {
        if (message == null || message.getBody() == null) throw new RuntimeException("消息不可为空");
        RabbitMessage<T> rabbitMessage;
        if (message instanceof RabbitMessage) {
            rabbitMessage = (RabbitMessage) message;
        } else {
            throw new RuntimeException("message type is wrong");
        }
        FailRetryEntityBuilder builder = FailRetryEntity.builder(rabbitMessage.getBusinessType(), rabbitMessage.getMessageId());
        try {
            rabbitTemplate.convertAndSend(
                    rabbitMessage.getExchangeName(),
                    rabbitMessage.getRoutingKey(),
                    JSON.toJSONString(rabbitMessage.getBody()),
                    correlationIdProcessor,
                    rabbitMessage.getCorrelation());
            logger.info("producer send message success,messageType:{},messageId:{},exchangeName:{},routingKey:{},messageBody:{}", rabbitMessage.getBusinessType(), rabbitMessage.getMessageId(), rabbitMessage.getExchangeName(), rabbitMessage.getRoutingKey(), JSON.toJSONString(message));
        } catch (Exception e) {
            //confirm模式的话 不做失败重试
            if (!rabbitMessage.isNeedConfirm()) {
                FailRetryEntity build = builder.fail().setFailReason(e.getMessage()).setMaxRetryTimes(5).setParams(JSON.toJSONString(rabbitMessage)).build();
                //todo 连续发送失败错误的提示
                failRetryService.processResult(build, null);
            }
            logger.error("send message error", e);
        }
    }

    /**
     * 事务提交后发送消息
     *
     * @param message
     * @return void
     */
    @Override
    public void sendMessageAfterTransactionCommitted(Message<T> message) {
        if (afterCommitExecutor == null) {
            throw new BusinessException("not support this operation");
        }
        afterCommitExecutor.execute(() -> this.sendMessage(message));
    }
}