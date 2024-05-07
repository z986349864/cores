package com.zd.core;

import com.alibaba.fastjson.JSONObject;
import com.zd.core.model.User;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@SpringBootTest
class CoreApplicationTests {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;


    @Test
    void redissonTest() {
        RLock lock = redissonClient.getLock("a");
        try {
            lock.lock();
            System.err.println("jia suo le... ");
        }finally {
            lock.unlock();
        }
    }
    @Test
    void contextLoads() {
        User user = new User();
        user.setId(1);
        user.setName("哈哈");
        String s = JSONObject.toJSONString(user);

        redisTemplate.opsForValue().set("a:b", s);
        String o = redisTemplate.opsForValue().get("a:b").toString();
        System.out.println(o);
    }

    @Test
    void get() {

        List<User> users = (List<User>) redisTemplate.opsForValue().get("redisson.com.zd.core.test.cache.UserAllCache_test!@#zhangSan");

        System.out.println(users);
    }

}
