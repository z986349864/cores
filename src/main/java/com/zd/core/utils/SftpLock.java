package com.zd.core.utils;

import java.util.concurrent.TimeUnit;

public interface SftpLock {

    boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime);
    void unlock(String lockKey);
}
