package com.tqz.multithread.transaction.demo.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 *
 * @version V1.0
 */
public class ExecutorConfig {
    private static final int MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private volatile static ExecutorService executorService;

    public static ExecutorService getThreadPool() {
        if (executorService == null) {
            synchronized (ExecutorConfig.class) {
                if (executorService == null) {
                    executorService = newThreadPool();
                }
            }
        }
        return executorService;
    }

    private static ExecutorService newThreadPool() {
        int queueSize = 500;
        int corePool = Math.min(5, MAX_POOL_SIZE);
        return new ThreadPoolExecutor(corePool, MAX_POOL_SIZE, 10000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize), new ThreadPoolExecutor.AbortPolicy());
    }

    private ExecutorConfig() {
    }
}