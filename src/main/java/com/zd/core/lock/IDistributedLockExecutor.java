package com.zd.core.lock;

import org.springframework.lang.NonNull;

public interface IDistributedLockExecutor {


    /**
     *  默认自动续约
     *
     * @param runnable
     * @return void
     */
    void runWithNoLeaseTimeLocked(Runnable runnable, @NonNull String... lockKey);
}
