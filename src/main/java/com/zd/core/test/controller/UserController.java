package com.zd.core.test.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zd.core.annotation.RedisCache;
import com.zd.core.config.BusinessQueueProperties;
import com.zd.core.constant.MessageConstants;
import com.zd.core.lock.IDistributedLockExecutor;
import com.zd.core.model.User;
import com.zd.core.mq.constant.FailRetryType;
import com.zd.core.mq.controller.BasicController;
import com.zd.core.mq.producer.IMessageProducer;
import com.zd.core.mq.producer.RabbitMessage;
import com.zd.core.response.Response;
import com.zd.core.test.cache.UserAllCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController extends BasicController {

    @Autowired
    private UserAllCache userAllCache;

    @Autowired
    private IDistributedLockExecutor distributedLockExecutor;

    @Autowired
    private IMessageProducer msgProducer;

    @Autowired
    private BusinessQueueProperties queueProperties;

    @RedisCache
    @GetMapping("/getCache")
    public List<User> getCache() {
        User user = new User();
        user.setName("ceshi22");
        user.setId(33);
        List<User> userList = Arrays.asList(user);
        return userList;
    }

    @GetMapping("/findUserTest")
    public Response getUserTest() {
        List<User> userList = userAllCache.get("ALL");
        String s = JSONObject.toJSONString(userList);
        distributedLockExecutor.runWithNoLeaseTimeLocked(() -> {
            System.err.println("..............需要上锁的业务代码块..........");
        }, String.join("redis:key"));
        return returnSuccess(s, MessageConstants.COMMON_USER_FIND_SUCCESS_MESSAGE);
    }

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
