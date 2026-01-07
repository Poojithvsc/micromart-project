package com.micromart.product.actuator;

import com.micromart.product.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Health Indicator for Inventory Status.
 * <p>
 * Spring Actuator: Custom HealthIndicator
 * <p>
 * Learning Points:
 * - Implement HealthIndicator interface
 * - Return Health.up() or Health.down() based on conditions
 * - Add custom details to health response
 * - Appears in /actuator/health endpoint
 */
@Component
@RequiredArgsConstructor
public class InventoryHealthIndicator implements HealthIndicator {

    private final InventoryRepository inventoryRepository;

    @Override
    public Health health() {
        try {
            long outOfStockCount = inventoryRepository.findOutOfStock().size();
            long reorderNeededCount = inventoryRepository.findNeedingReorder().size();

            // Determine health status
            if (outOfStockCount > 10) {
                return Health.down()
                        .withDetail("status", "CRITICAL")
                        .withDetail("outOfStockProducts", outOfStockCount)
                        .withDetail("productsNeedingReorder", reorderNeededCount)
                        .withDetail("message", "High number of out-of-stock products")
                        .build();
            }

            if (reorderNeededCount > 20) {
                return Health.status("WARN")
                        .withDetail("status", "WARNING")
                        .withDetail("outOfStockProducts", outOfStockCount)
                        .withDetail("productsNeedingReorder", reorderNeededCount)
                        .withDetail("message", "Many products need reorder")
                        .build();
            }

            return Health.up()
                    .withDetail("status", "OK")
                    .withDetail("outOfStockProducts", outOfStockCount)
                    .withDetail("productsNeedingReorder", reorderNeededCount)
                    .withDetail("message", "Inventory levels are healthy")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "ERROR")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
