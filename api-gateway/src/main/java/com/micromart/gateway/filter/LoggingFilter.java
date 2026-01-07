package com.micromart.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Global Logging Filter for request tracing and monitoring.
 * <p>
 * This filter:
 * - Generates a unique correlation ID for each request
 * - Logs incoming requests with method, path, and headers
 * - Logs response status and execution time
 * - Adds correlation ID to request headers for distributed tracing
 */
@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = Instant.now().toEpochMilli();

        // Generate or extract correlation ID
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        // Log incoming request
        log.info("Incoming request: {} {} | CorrelationId: {} | Client: {}",
                request.getMethod(),
                request.getURI().getPath(),
                correlationId,
                request.getRemoteAddress());

        // Add correlation ID to request headers
        String finalCorrelationId = correlationId;
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    long duration = Instant.now().toEpochMilli() - startTime;
                    log.info("Outgoing response: {} {} | Status: {} | Duration: {}ms | CorrelationId: {}",
                            request.getMethod(),
                            request.getURI().getPath(),
                            exchange.getResponse().getStatusCode(),
                            duration,
                            finalCorrelationId);
                }));
    }

    @Override
    public int getOrder() {
        return -200; // Run before authentication filter
    }
}
