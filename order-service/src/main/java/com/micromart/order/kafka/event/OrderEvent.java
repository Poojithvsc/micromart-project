package com.micromart.order.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Event for Kafka.
 * <p>
 * This is the event published to Kafka when order status changes.
 * Other services (like Product Service for inventory) consume this.
 * <p>
 * Learning Points:
 * - Events should be self-contained (include all necessary data)
 * - Use immutable data structures
 * - Include timestamp and event type for auditing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String eventType;
    private String orderNumber;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private List<OrderItemEvent> items;
    private LocalDateTime occurredAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }

    // Event type constants
    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_CONFIRMED = "ORDER_CONFIRMED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String ORDER_SHIPPED = "ORDER_SHIPPED";
    public static final String ORDER_DELIVERED = "ORDER_DELIVERED";
}
