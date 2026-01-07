package com.micromart.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Application Configuration demonstrating various conditional annotations.
 * <p>
 * Spring Annotations demonstrated:
 * - @Configuration: Marks class as source of bean definitions
 * - @Bean: Marks methods as bean producers
 * - @ConditionalOnProperty: Bean created based on property value
 * - @ConditionalOnMissingBean: Bean created only if no other exists
 * - @Profile: Bean created only for specific profiles
 * - @Value: Property injection
 * - @EnableAsync: Enables async method execution
 * <p>
 * PEAA Pattern: Plugin
 * Different beans are "plugged in" based on configuration.
 */
@Configuration
@EnableAsync
@Slf4j
public class AppConfig {

    /**
     * Application environment name - injected from properties.
     */
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Custom thread pool for async operations.
     * <p>
     * @ConditionalOnProperty: Only created if async.enabled=true
     * This bean demonstrates conditional bean creation based on properties.
     */
    @Bean(name = "asyncExecutor")
    @ConditionalOnProperty(
            name = "async.enabled",
            havingValue = "true",
            matchIfMissing = true  // Default to true if property missing
    )
    public Executor asyncExecutor() {
        log.info("Creating async executor for profile: {}", activeProfile);
        return Executors.newFixedThreadPool(10);
    }

    /**
     * Fallback executor when async is disabled.
     * <p>
     * @ConditionalOnMissingBean: Created only if no other Executor bean exists.
     * This demonstrates fallback bean pattern.
     */
    @Bean(name = "fallbackExecutor")
    @ConditionalOnMissingBean(Executor.class)
    public Executor fallbackExecutor() {
        log.info("Creating fallback single-thread executor");
        return Executors.newSingleThreadExecutor();
    }

    /**
     * Development-only configuration bean.
     * <p>
     * @Profile("dev"): Only created in development profile.
     */
    @Bean
    @Profile("dev")
    public DevToolsConfig devToolsConfig() {
        log.info("Loading development tools configuration");
        return new DevToolsConfig(true, true, true);
    }

    /**
     * Production configuration bean with stricter settings.
     * <p>
     * @Profile("prod"): Only created in production profile.
     */
    @Bean
    @Profile("prod")
    public DevToolsConfig prodToolsConfig() {
        log.info("Loading production configuration (strict mode)");
        return new DevToolsConfig(false, false, false);
    }

    /**
     * Default configuration when no profile is active.
     * <p>
     * @Profile("default"): Fallback for unspecified profile.
     */
    @Bean
    @Profile("default")
    public DevToolsConfig defaultToolsConfig() {
        log.info("Loading default configuration");
        return new DevToolsConfig(true, false, false);
    }

    /**
     * Configuration record for development tools.
     * Demonstrates using record as configuration holder.
     */
    public record DevToolsConfig(
            boolean showSql,
            boolean enableSwagger,
            boolean detailedErrors
    ) {
    }
}
