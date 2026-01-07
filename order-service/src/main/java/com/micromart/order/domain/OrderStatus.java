package com.micromart.order.domain;

/**
 * Order Status Enumeration.
 * <p>
 * Represents the lifecycle states of an order.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
