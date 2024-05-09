package com.zd.core.test.consumer;

import com.zd.core.model.User;
import com.zd.core.mq.constant.FailRetryType;
import com.zd.core.mq.consumer.AbstractBaseConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * User测试
 */
@Component
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "userQueueName")
public class UserConsumer extends AbstractBaseConsumer<List<User>> {


    public UserConsumer() {
        super(FailRetryType.USER_ALL_FAIL);
    }

    @Override
    public void consumer(List<User> userList) {
        userList.forEach(u -> System.err.println("======================="+ u +"===================="));

    }
}