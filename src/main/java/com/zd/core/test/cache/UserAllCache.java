package com.zd.core.test.cache;

import com.zd.core.cache.redis.StringRedisCache;
import com.zd.core.model.User;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAllCache extends StringRedisCache<List<User>> {
    @Autowired
    private UserAllCacheProvider userAllCacheProvider;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void afterPropertiesSet() {
        super.setCacheProvider(userAllCacheProvider);
        super.setRedissonClient(redissonClient);
        //一天超时
        setTimeOut(24 * 60 * 60);
    }
}
