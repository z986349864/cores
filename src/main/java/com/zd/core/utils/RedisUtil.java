package com.zd.core.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    public String get(String key) {
        if (StringUtils.isBlank(key)) {
            return StringUtils.EMPTY;
        }
        try {
            ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
            return valueOperations.get(key);
        } catch (Exception e) {
            log.error("redis get报错", e);
            throw e;
        }
    }

    public <T> T get(String key, Type valueType) {
        String value = get(key);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return JSON.parseObject(value, valueType);
    }

    public <T> List<T> getForList(String key, Class<T> clazz) {
        String value = get(key);
        return StringUtils.isBlank(value) ? Collections.emptyList() : JSON.parseArray(value, clazz);
    }

    public void set(String key, String value, int expireSecond) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        try {
            ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
            if (expireSecond <= 0) {
                valueOperations.set(key, value);
            } else {
                valueOperations.set(key, value, expireSecond, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(String.format("redis set报错：%s，%s，%s", key, value, expireSecond), e);
            throw e;
        }
    }

    public void set(String key, Object obj, int expireSecond) {
        String value = JSON.toJSONString(obj);
        set(key, value, expireSecond);
    }

    public void del(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("redis del报错", e);
            throw e;
        }
    }
}
