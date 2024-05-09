package com.zd.core.config;

import com.zd.core.test.consumer.UserConsumer;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName：RabbitMQAutoConfiguration
 * @Description: MQ的自动配置
 * @Version V1.0
 **/
@Configuration
@EnableConfigurationProperties({RabbitProperties.class})
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitMQAutoConfiguration {

    /**
     * 创建RabbitMQ连接工厂,用于创建非单例的RabbitTemplate时使用
     *
     * @return
     */
    @Autowired
    private BusinessQueueProperties queueProperties;
    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private SimpleRabbitListenerContainerFactory containerFactory;

    /**
     * 声明一个持久化的直连交换机
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.rabbitmq", name = "exchangeName")
    public DirectExchange directExchange() {
        return new DirectExchange(queueProperties.getExchangeName(),
                true, false);
    }

    /**
     * 声明 用户信息的队列
     *
     * @return
     */
    @Bean(name = "userQueueName")
    @ConditionalOnProperty(prefix = "spring.rabbitmq", name = "userQueueName")
    public Queue skuSyncBackListingQueue() {
        return QueueBuilder
                .durable(queueProperties.getUserQueueName())
                .build();
    }

    /**
     * 声明绑定关系
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.rabbitmq", name = "userRoutingKey")
    public Binding userQueueBinding() {
        return BindingBuilder
                .bind(skuSyncBackListingQueue())
                .to(directExchange())
                .with(queueProperties.getUserRoutingKey());
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.rabbitmq", name = "userQueueName")
    public SimpleMessageListenerContainer skuSyncBackListenerContainer(UserConsumer consumer) {
        SimpleMessageListenerContainer container = containerFactory.createListenerContainer();
        container.setPrefetchCount(5);
        container.setQueues(skuSyncBackListingQueue());
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(consumer);
        return container;
    }
}
