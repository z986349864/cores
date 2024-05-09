package com.zd.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class BusinessQueueProperties {

    /**
     * 交换机名称
     */
    private String exchangeName;
    /**
     * 同步用户信息QueueName
     */
    private String userQueueName;
    /**
     * 同步用户信息RoutingKey
     */
    private String userRoutingKey;
}
