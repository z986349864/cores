package com.zd.core.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.zd.core.exception.BusinessException;
import com.zd.core.mq.failretry.builder.FailRetryEntityBuilder;
import com.zd.core.mq.failretry.entity.FailRetryEntity;
import com.zd.core.mq.failretry.entity.IRetryResultProcessStrategy;
import com.zd.core.mq.failretry.service.IFailRetryService;
import com.zd.core.utils.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import java.util.Collection;

public abstract class AbstractBaseConsumer<T> implements ChannelAwareMessageListener, IConsumer<T>, InitializingBean {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    protected IFailRetryService failRetryService;

    @Autowired(required = false)
    private IRetryResultProcessStrategy retryResultProcessStrategy;

    private final Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();

    /**
     * 消息消费失败的重试类型
     */
    private final Integer failRetryType;

    public AbstractBaseConsumer(Integer failRetryType) {
        this.failRetryType = failRetryType;
    }

    /**
     * 消费数据
     *
     * @param message CorrelationId等同于业务的唯一id
     * @param channel
     * @return void
     */
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        String id = message.getMessageProperties().getCorrelationId();
        FailRetryEntityBuilder builder = FailRetryEntity.builder(failRetryType, id);
        String jsonData = (String) jackson2JsonMessageConverter.fromMessage(message);
        logger.info("consumer stating, messageId:{},queueName:{},routingKey:{},exchange:{},,messageBody:{}",
                id,
                message.getMessageProperties().getConsumerQueue(),
                message.getMessageProperties().getReceivedRoutingKey(),
                message.getMessageProperties().getReceivedExchange(),
                jsonData);
        try {
            ResolvableType resolvableType = ResolvableType.forClass(getClass());
            Class<?> clazz = resolvableType.getSuperType().getGeneric(0).resolve();
            //当前是一个泛型集合
            if (Collection.class.isAssignableFrom(clazz)) {
                Class<?> actualClazz = resolvableType.getSuperType().getGeneric(0).getGeneric(0).resolve();
                if (actualClazz == null) {
                    throw new BusinessException("this message generic type not support");
                }
                T listData = (T) JSON.parseArray(jsonData, actualClazz);
                consumer(listData);
            } else {
                T data = (T) JSON.parseObject(jsonData, clazz);
                consumer(data);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            logger.info("consumer receive success messageId:{} messageType:{},messageBody:{}", id, failRetryType, jsonData);
        } catch (Exception e) {
            logger.error("consumer process failed messageId:{},messageBody:{}", message.getMessageProperties().getCorrelationId(), jsonData, e);
            if (builder != null) {
                FailRetryEntity entity = builder.fail()
                        .setFailReason(StringUtils.isBlank(e.getMessage()) ? ExceptionUtils.getStack(e, 4000) : e.getMessage())
                        .setParams(jsonData)
                        // 最大重试数量 默认返回5次
                        .setMaxRetryTimes(this.getRetriesNumber())
                        .build();
                entity.setRetryTimeStrategy(this.getNextRetryTimeStrategy());

                //注册通用
                failRetryService.processResult(entity, this.registeredRetryResultProcessStrategy());
                //错误之后直接拒收消息
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (!ConsumerRetryService.getBusinessConsumerContainer().contains(failRetryType)) {
            synchronized (ConsumerRetryService.class) {
                if (!ConsumerRetryService.getBusinessConsumerContainer().contains(failRetryType)) {
                    ConsumerRetryService.getBusinessConsumerContainer().put(failRetryType, getClass());
                }
            }
        }
    }

    @Override
    public IRetryResultProcessStrategy registeredRetryResultProcessStrategy() {
        return retryResultProcessStrategy;
    }

}
