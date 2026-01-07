package com.micromart.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Application Configuration.
 * <p>
 * Spring Concepts:
 * - @EnableAsync: Enables asynchronous method execution
 * - ThreadPoolTaskExecutor: Custom thread pool for @Async methods
 * <p>
 * Learning Points:
 * - @EnableAsync enables the @Async annotation
 * - Custom executor allows control over thread pool size
 * - Useful for non-blocking operations (notifications, logging, etc.)
 */
@Configuration
@EnableAsync
public class AppConfig {

    /**
     * Custom thread pool for async operations.
     * <p>
     * Why configure this:
     * - Control max concurrent async tasks
     * - Prevent unbounded thread creation
     * - Set meaningful thread names for debugging
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size: minimum threads always alive
        executor.setCorePoolSize(2);

        // Max pool size: maximum threads during high load
        executor.setMaxPoolSize(10);

        // Queue capacity: tasks waiting when all threads busy
        executor.setQueueCapacity(500);

        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("ProductAsync-");

        // Initialize the executor
        executor.initialize();

        return executor;
    }
}
