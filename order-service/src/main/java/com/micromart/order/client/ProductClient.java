package com.micromart.order.client;

import com.micromart.order.client.dto.ProductDto;
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
 * - Circuit breaker fallback
 */
@FeignClient(
        name = "product-service",
        fallback = ProductClientFallback.class,
        path = "/products"
)
public interface ProductClient {

    @GetMapping("/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);

    @GetMapping("/{id}/price")
    BigDecimal getProductPrice(@PathVariable("id") Long id);
}
