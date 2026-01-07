package com.micromart.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fallback implementation for ProductClient.
 * <p>
 * Circuit Breaker Pattern: Provides graceful degradation when
 * the product service is unavailable.
 */
@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public BigDecimal getProductPrice(Long id) {
        log.warn("Fallback: Product service unavailable, returning default price for product {}", id);
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isProductAvailable(Long id) {
        log.warn("Fallback: Product service unavailable, assuming product {} is unavailable", id);
        return false;
    }
}
