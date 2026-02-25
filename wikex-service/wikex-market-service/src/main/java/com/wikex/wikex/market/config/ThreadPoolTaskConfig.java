package com.wikex.wikex.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Thread pool configuration
 * @author zhh
 */
@Configuration
@EnableAsync
public class ThreadPoolTaskConfig {

    /** 
     * By default, after creating a thread pool, the number of threads is 0.
     * When a task arrives, a thread will be created to execute it.
     * Once the number of threads reaches corePoolSize, new tasks are placed into the queue.
     * When the queue is full, new threads are created.
     * Once the number of threads reaches or exceeds maxPoolSize, the rejection policy will be applied.
     */

    /** Core number of threads (default thread count) */
    private static final int corePoolSize = 20;
    /** Maximum number of threads */
    private static final int maxPoolSize = 100;
    /** Idle thread keep-alive time (in seconds) */
    private static final int keepAliveTime = 10;
    /** Queue capacity */
    private static final int queueCapacity = 200;
    /** Thread name prefix */
    private static final String threadNamePrefix = "Async-Service-";

    @Bean("taskExecutor") // Bean name, default is method name with lowercase initial
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveTime);
        executor.setThreadNamePrefix(threadNamePrefix);
        // Rejection policy for tasks:
        // CallerRunsPolicy: the calling thread (the thread that submitted the task) executes the task
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // Initialize the thread pool
        executor.initialize();
        return executor;
    }
}
