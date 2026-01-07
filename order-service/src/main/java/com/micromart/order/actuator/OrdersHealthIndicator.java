package com.micromart.order.actuator;

import com.micromart.order.domain.OrderStatus;
import com.micromart.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Custom Health Indicator for Order Processing Status.
 * <p>
 * Spring Actuator: Custom HealthIndicator
 * <p>
 * Monitors for stale pending orders that might indicate processing issues.
 */
@Component
@RequiredArgsConstructor
public class OrdersHealthIndicator implements HealthIndicator {

    private final OrderRepository orderRepository;

    @Override
    public Health health() {
        try {
            // Count pending orders
            long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);

            // Check for stale orders (pending for more than 1 hour)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long staleOrderCount = orderRepository.findStalePendingOrders(oneHourAgo).size();

            // Build health response
            if (staleOrderCount > 10) {
                return Health.down()
                        .withDetail("status", "CRITICAL")
                        .withDetail("pendingOrders", pendingCount)
                        .withDetail("staleOrders", staleOrderCount)
                        .withDetail("message", "High number of stale pending orders")
                        .build();
            }

            if (staleOrderCount > 0) {
                return Health.status("WARN")
                        .withDetail("status", "WARNING")
                        .withDetail("pendingOrders", pendingCount)
                        .withDetail("staleOrders", staleOrderCount)
                        .withDetail("message", "Some orders are stale")
                        .build();
            }

            return Health.up()
                    .withDetail("status", "OK")
                    .withDetail("pendingOrders", pendingCount)
                    .withDetail("staleOrders", 0)
                    .withDetail("message", "Order processing is healthy")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "ERROR")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
