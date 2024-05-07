package com.zd.core.cache.redis;

import com.zd.core.cache.ICacheDataHashProvider;
import com.zd.core.cache.ICacheHash;
import com.zd.core.cache.constant.NullObject;
import com.zd.core.cache.exception.RedisException;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

public class HashRedisCache<V> implements ICacheHash<String, V>, InitializingBean {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 数据提供者
     */
    protected ICacheDataHashProvider<String, V> cacheProvider;

    /**
     * 数据存储器
     */
    protected RedissonClient redissonClient;

    /**
     * 超时时间,单位秒,默认10分钟
     */
    protected int timeOut = 10 * 60;


    /**
     * 获取缓存
     *
     * @param key 缓存Key
     * @return 缓存Value
     */

    @Override
    public V getHash(String key, String hashKey) {
        if (StringUtils.isEmpty(key)) {
            logger.warn("[" + key + "] key is empty, return null");
            return null;
        }
        V value = null;
        try {
            value = (V) redissonClient.getMap(key).get(hashKey);
            //如果key不存在，从缓存提供者获取，再放到缓存中
            if (value == null) {
                value = cacheProvider.getHash(key, hashKey);
                logger.warn("[" + key + "] not found in cache, read from cache data provider");
                if (value == null) {
                    RMap map = redissonClient.getMap(key);
                    map.put(hashKey, new NullObject());
                    map.expire(timeOut, TimeUnit.SECONDS);
                } else {
                    RMap map = redissonClient.getMap(key);
                    map.put(hashKey, value);
                    map.expire(timeOut, TimeUnit.SECONDS);
                }
            }
        } catch (RedisException e) {
            logger.warn("[" + key + "] not found in cache:", e.getMessage());
            //key存在，value为空串
            return null;
        } catch (Exception e) {
            //其他异常
            logger.error("[" + key + "] get data from cache fail, read from cache data provider", e);
            value = cacheProvider.getHash(key, hashKey);
            return value;
        }
        return value;
    }

    /**
     * 清除掉指定缓存
     *
     * @param key
     * @param hashKey
     */
    @Override
    public void invalid(String key, String hashKey) {
        redissonClient.getMap(key).remove(hashKey);
    }

    /**
     * 清除掉整个hash缓存
     *
     * @param key
     */
    @Override
    public void invalid(String key) {
        redissonClient.getMap(key).delete();
    }

    /**
     * 根据uuid和key生成key
     *
     * @param key
     * @return
     * @see
     */
    private String generateCacheKey(String key) {
        return "redisson." + getCacheType() + "_" + key;
    }

    /**
     * 获取缓存类型
     *
     * @return
     */
    private String getCacheType() {
        return getClass().getName();
    }

    public ICacheDataHashProvider<String, V> getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(ICacheDataHashProvider<String, V> cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.cacheProvider == null) {
            throw new RedisException(getCacheType() + " does not define cache data provider");
        }
    }
}
