package com.micromart.order.actuator;

import com.micromart.order.client.ProductClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Health Indicator for External Service Dependencies.
 * <p>
 * Checks connectivity to Product Service via Feign client.
 * <p>
 * Learning Points:
 * - Health indicators can check external dependencies
 * - Useful for monitoring microservice connectivity
 * - Should use lightweight checks (e.g., dedicated health endpoints)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private final ProductClient productClient;

    @Override
    public Health health() {
        Health.Builder builder = Health.up();

        // Check Product Service connectivity
        boolean productServiceUp = checkProductService();

        if (!productServiceUp) {
            builder.down();
        }

        return builder
                .withDetail("productService", productServiceUp ? "UP" : "DOWN")
                .build();
    }

    private boolean checkProductService() {
        try {
            // Try to get a product price (will use fallback if service down)
            // In production, you'd have a dedicated /health endpoint
            productClient.getProductPrice(1L);
            return true;
        } catch (Exception e) {
            log.warn("Product service health check failed: {}", e.getMessage());
            return false;
        }
    }
}
