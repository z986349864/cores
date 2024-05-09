package com.zd.core.test.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zd.core.config.BusinessQueueProperties;
import com.zd.core.model.User;
import com.zd.core.mq.constant.FailRetryType;
import com.zd.core.mq.producer.IMessageProducer;
import com.zd.core.mq.producer.RabbitMessage;
import com.zd.core.test.cache.UserAllCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserAllCache userAllCache;

    @Autowired
    private IMessageProducer msgProducer;

    @Autowired
    private BusinessQueueProperties queueProperties;


    @GetMapping("/getUserAll")
    public String getUserAll() {
        List<User> userList = userAllCache.get("ALL");
        String s = JSONObject.toJSONString(userList);
        return s;
    }

    @GetMapping("/sendMsg")
    public String sendMsg() {
        String id = UUID.randomUUID().toString();
        List<User> userList = userAllCache.get("ALL");
        RabbitMessage<List<User>> msg = new RabbitMessage<>(
                id,
                FailRetryType.USER_ALL_FAIL,
                userList,
                queueProperties.getExchangeName(),
                queueProperties.getUserRoutingKey()
        );
        msgProducer.sendMessageAfterTransactionCommitted(msg);

        return "success";
    }

}
