package com.micromart.order.client;

import com.micromart.order.client.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fallback for Product Client.
 * <p>
 * Circuit Breaker Pattern (Resilience4j):
 * When the product service is unavailable, this fallback is used.
 * <p>
 * This demonstrates fault tolerance in microservices.
 * <p>
 * Learning Points:
 * - Fallback provides degraded functionality when service unavailable
 * - Should log the failure for monitoring
 * - Returns sensible defaults or throws custom exceptions
 */
@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductDto getProduct(Long id) {
        log.warn("Product service unavailable - fallback for getProduct({})", id);
        // Return null or throw exception - order service should handle this
        return null;
    }

    @Override
    public BigDecimal getProductPrice(Long id) {
        log.warn("Product service unavailable - fallback for getProductPrice({})", id);
        // Return a default value or throw a handled exception
        return BigDecimal.ZERO;
    }
}
