package com.zd.core.mq.producer;

public class Message<T> {

    public Message(String messageId, Integer businessType, T body) {
        this.messageId = messageId;
        this.businessType = businessType;
        this.body = body;
    }

    /**
     * 消息id
     */
    private String messageId;

    /**
     * 业务类型 会用作重试时 失败表的businessType
     */
    private Integer businessType;

    /**
     * 消息体
     */
    private T body;

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}