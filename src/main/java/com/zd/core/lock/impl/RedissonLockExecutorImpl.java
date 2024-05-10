package com.zd.core.lock.impl;

import com.alibaba.fastjson.JSON;
import com.zd.core.lock.IDistributedLockExecutor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
public class RedissonLockExecutorImpl implements IDistributedLockExecutor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Integer waitTime = 10;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 带锁执行
     *
     * @param
     * @param runnable
     * @return void
     */
    @Override
    public void runWithNoLeaseTimeLocked(Runnable runnable, String... lockKey) {
        RLock lock = this.getLock(lockKey);
        if (lock != null) {
            try {
                if (lock.tryLock(waitTime, -1, TimeUnit.SECONDS)) {
                    logger.info("get lock success,lockKey:{}", JSON.toJSONString(lockKey));
                    runnable.run();
                    logger.info("execute success,lockKey:{}", JSON.toJSONString(lockKey));
                } else {
                    throw new RuntimeException("get lock failed,lockKey:" + JSON.toJSONString(lockKey));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                    this.releaseLocke(lock,lockKey);
                } else {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCompletion(int status) {
                            releaseLocke(lock, lockKey);
                        }
                    });
                }
            }
        } else {
            throw new IllegalArgumentException("this lock not support");
        }
    }

    /**
     * 释放锁
     *
     * @param lock
     */
    private void releaseLocke(RLock lock, String... lockKey) {
        try {
            if (lock.isHeldByCurrentThread() && lock.isLocked()) {
                lock.unlock();
                logger.info("the single lock release success,lockKey:{}", JSON.toJSONString(lockKey));
            }
        } catch (UnsupportedOperationException e) {
            lock.unlock();
            logger.info("the lock has released, but it may be multiLock and the lock not support isHeldByCurrentThread method or isLocked method,lockKey:{},msg:{}", JSON.toJSONString(lockKey), e.getMessage());
        }
    }

    /**
     * 获取锁实例
     *
     * @param lockKey
     * @return
     */
    public RLock getLock(String... lockKey) {
        if (lockKey == null || lockKey.length == 0) {
            throw new IllegalArgumentException("the lock key can't be empty");
        }
        if (lockKey.length == 1) {
            return redissonClient.getLock(MessageFormat.format("{0}_{1}", getClass(), lockKey[0]));
        } else {
            List<RLock> locks = new ArrayList<>();
            for (String key : lockKey) {
                locks.add(redissonClient.getLock(MessageFormat.format("{0}_{1}", getClass(), key)));
            }
            return redissonClient.getMultiLock(locks.toArray(new RLock[]{}));
        }
    }
}
