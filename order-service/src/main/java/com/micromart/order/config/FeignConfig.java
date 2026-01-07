package com.micromart.order.config;

import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign Client Configuration.
 * <p>
 * Spring Cloud: Feign customization
 * <p>
 * Learning Points:
 * - Logger.Level controls Feign logging verbosity
 * - Retryer configures retry behavior for failed requests
 * - Can also configure encoders, decoders, interceptors
 */
@Configuration
public class FeignConfig {

    /**
     * Feign logger level.
     * NONE: No logging (default)
     * BASIC: Log method, URL, response status, execution time
     * HEADERS: BASIC + request/response headers
     * FULL: HEADERS + request/response bodies
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Retry configuration for Feign clients.
     * period: Initial interval between retries
     * maxPeriod: Maximum interval between retries (exponential backoff)
     * maxAttempts: Maximum number of attempts (1 = no retry)
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                100,  // period (ms)
                TimeUnit.SECONDS.toMillis(1),  // maxPeriod
                3  // maxAttempts
        );
    }
}
