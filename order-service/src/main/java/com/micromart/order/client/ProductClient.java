package com.micromart.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

/**
 * Product Service Feign Client.
 * <p>
 * Spring Cloud: @FeignClient
 * Declarative REST client that uses Eureka for service discovery.
 * The name "product-service" matches the application name registered in Eureka.
 * <p>
 * This demonstrates:
 * - Inter-service communication
 * - Service discovery integration
 * - Declarative REST client pattern
 */
@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/products/{id}/price")
    BigDecimal getProductPrice(@PathVariable Long id);

    @GetMapping("/products/{id}/available")
    boolean isProductAvailable(@PathVariable Long id);
}
