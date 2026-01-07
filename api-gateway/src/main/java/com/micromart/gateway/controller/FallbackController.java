package com.micromart.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller for Circuit Breaker responses.
 * <p>
 * When a downstream service is unavailable and the circuit breaker opens,
 * requests are redirected to these fallback endpoints to provide graceful
 * degradation instead of error responses.
 * <p>
 * Circuit Breaker Pattern:
 * Prevents cascade failures by stopping requests to failing services
 * and returning fallback responses instead.
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    /**
     * Fallback for User Service.
     */
    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        log.warn("User service is unavailable - returning fallback response");
        return Mono.just(createFallbackResponse("User Service"));
    }

    /**
     * Fallback for Product Service.
     */
    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        log.warn("Product service is unavailable - returning fallback response");
        return Mono.just(createFallbackResponse("Product Service"));
    }

    /**
     * Fallback for Order Service.
     */
    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        log.warn("Order service is unavailable - returning fallback response");
        return Mono.just(createFallbackResponse("Order Service"));
    }

    /**
     * Create a standardized fallback response.
     */
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", serviceName + " is currently unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", serviceName);
        response.put("fallback", true);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
