package com.zd.core.mq.producer;

import com.zd.core.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitMessage<T> extends Message<T> {

    public RabbitMessage(String messageId, Integer businessType, T body, String exchangeName, String routingKey) {
        super(messageId, businessType, body);
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.needConfirm = false;
        this.correlation = new CorrelationData(messageId);
        //验证所有的消息是否符合规范
        verifyMessage();
    }

    /**
     * 交换机名称
     */
    private String exchangeName;
    /**
     * 路由建
     */
    private String routingKey;

    /**
     * 是否要启用confirm
     */
    private boolean needConfirm;

    /**
     * confirm
     */
    private RabbitTemplate.ConfirmCallback confirmCallback;

    /**
     * 消息的
     */
    private CorrelationData correlation;

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public boolean isNeedConfirm() {
        return needConfirm;
    }

    public void setNeedConfirm(boolean needConfirm) {
        this.needConfirm = needConfirm;
    }

    public RabbitTemplate.ConfirmCallback getConfirmCallback() {
        return confirmCallback;
    }

    public void setConfirmCallback(RabbitTemplate.ConfirmCallback confirmCallback) {
        this.confirmCallback = confirmCallback;
    }

    public CorrelationData getCorrelation() {
        return correlation;
    }

    public void setCorrelation(CorrelationData correlation) {
        this.correlation = correlation;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    private void verifyMessage() {
        if (StringUtils.isBlank(this.getMessageId())) {
            throw new BusinessException("messageId can not be empty");
        }
        if (null == this.getBusinessType()) {
            throw new BusinessException("businessType can not be null");
        }
        if (null == this.getBody()) {
            throw new BusinessException("message body can not be null");
        }
        if (StringUtils.isBlank(this.getExchangeName())) {
            throw new BusinessException("exchangeName can not be empty");
        }
        if (StringUtils.isBlank(this.getRoutingKey())) {
            throw new BusinessException("routingKey can not be empty");
        }
    }
}
