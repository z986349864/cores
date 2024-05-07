package com.zd.core.cache.redis;

import com.zd.core.cache.ICache;
import com.zd.core.cache.ICacheDataProvider;
import com.zd.core.cache.constant.CacheConstants;
import com.zd.core.cache.constant.NullObject;
import com.zd.core.cache.exception.RedisException;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

public class StringRedisCache<V> implements ICache<String, V>, InitializingBean {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 数据提供者
     */
    protected ICacheDataProvider<String, V> cacheProvider;

    /**
     * 数据存储器
     */
    protected RedissonClient redissonClient;

    /**
     * 超时时间,单位秒,默认30秒
     */
    protected int timeOut = CacheConstants.TTL_DEFAULT_EXPIRED_TIME;


    /**
     * 获取缓存
     *
     * @param key 缓存Key
     * @return 缓存Value
     */
    @Override
    public V get(String key) {
        if(StringUtils.isEmpty(key)) {
            logger.warn("["+ generateCacheKey(key) +"] key is empty, return null");
            //key存在，value为空串
            return null;
        }
        V value = null;
        RBucket bucket = redissonClient.getBucket(generateCacheKey(key));
        try {
            value = (V) bucket.get();
            if (value == null) {
                value = cacheProvider.get(key);
                logger.warn("["+ generateCacheKey(key) +"] not found in cache, read from cache data provider");
                if (value == null) {
                    bucket.set(new NullObject(), timeOut, TimeUnit.SECONDS);
                } else {
                    bucket.set(value, timeOut, TimeUnit.SECONDS);
                }
            } else if (value instanceof NullObject) {
                //如果缓存的数据是NullObject，返回空
                value = null;
            }
        } catch (Exception e){
            //其他异常
            logger.error("["+ generateCacheKey(key) +"] get data from cache fail, read from cache data provider");
            logger.debug("["+ generateCacheKey(key) +"] get data from cache fail, read from cache data provider", e);
            try {
                value = cacheProvider.get(key);
            } catch (Exception e2) {
                logger.error("Get data from cache provider fail");
                logger.debug("Get data from cache provider fail", e2);
                return null;
            }
        }
        return value;
    }

    /**
     * 失效key对应的缓存
     *
     * @param key
     */
    @Override
    public void invalid(String key) {
        redissonClient.getBucket(generateCacheKey(key)).delete();
    }

    /**
     * 根据uuid和key生成key
     * @param key
     * @return
     * @see
     */
    private String generateCacheKey(String key) {
        return "redisson." + getCacheType() + "_" + key;
    }

    /**
     * 获取缓存类型
     * @return
     */
    private String getCacheType() {
        return getClass().getName();
    }

    public ICacheDataProvider<String, V> getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(ICacheDataProvider<String, V> cacheProvider) {
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
