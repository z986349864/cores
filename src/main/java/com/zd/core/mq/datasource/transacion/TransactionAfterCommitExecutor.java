package com.zd.core.mq.datasource.transacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class TransactionAfterCommitExecutor {

    public TransactionAfterCommitExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    private Logger logger = LoggerFactory.getLogger(TransactionAfterCommitExecutor.class);

    /**
     * 当前线程池
     */
    private final TaskExecutor taskExecutor;

    /**
     * @param runnable 异步执行
     */
    public void executeAsync(Runnable runnable) {
        doExecute(runnable, true);
    }

    /**
     * 同步执行
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        doExecute(runnable, false);
    }

    /**
     * 执行任务
     *
     * @param runnable 当前需要执行的任务
     * @param isAsync  是否需要异步执行 异步取决于两个条件 taskExecutor配置了taskExecutor
     * @return void
     */
    private void doExecute(Runnable runnable, boolean isAsync) {
        if (runnable == null) {
            throw new RuntimeException("runnable can not be  null");
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            if (isAsync && taskExecutor != null) {
                taskExecutor.execute(runnable);
            } else {
                runnable.run();
            }
        } else {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    logger.info("afterCommit runnable has registered");
                    if (isAsync && taskExecutor != null) {
                        taskExecutor.execute(runnable);
                    } else {
                        runnable.run();
                    }
                }
            });
        }
    }

}
