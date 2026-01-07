package com.micromart.common.event;

/**
 * Constants for Kafka event types used across microservices.
 * <p>
 * Using constants ensures consistency in event type naming
 * and prevents typos in event routing.
 */
public final class EventType {

    private EventType() {
        // Utility class - prevent instantiation
    }

    // ========================================================================
    // Order Events
    // ========================================================================
    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_UPDATED = "ORDER_UPDATED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String ORDER_COMPLETED = "ORDER_COMPLETED";
    public static final String ORDER_PAYMENT_PENDING = "ORDER_PAYMENT_PENDING";
    public static final String ORDER_PAYMENT_COMPLETED = "ORDER_PAYMENT_COMPLETED";
    public static final String ORDER_PAYMENT_FAILED = "ORDER_PAYMENT_FAILED";
    public static final String ORDER_SHIPPED = "ORDER_SHIPPED";
    public static final String ORDER_DELIVERED = "ORDER_DELIVERED";

    // ========================================================================
    // Inventory Events
    // ========================================================================
    public static final String INVENTORY_RESERVED = "INVENTORY_RESERVED";
    public static final String INVENTORY_RELEASED = "INVENTORY_RELEASED";
    public static final String INVENTORY_UPDATED = "INVENTORY_UPDATED";
    public static final String INVENTORY_LOW_STOCK = "INVENTORY_LOW_STOCK";
    public static final String INVENTORY_OUT_OF_STOCK = "INVENTORY_OUT_OF_STOCK";

    // ========================================================================
    // User Events
    // ========================================================================
    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String USER_PASSWORD_CHANGED = "USER_PASSWORD_CHANGED";

    // ========================================================================
    // Product Events
    // ========================================================================
    public static final String PRODUCT_CREATED = "PRODUCT_CREATED";
    public static final String PRODUCT_UPDATED = "PRODUCT_UPDATED";
    public static final String PRODUCT_DELETED = "PRODUCT_DELETED";
    public static final String PRODUCT_PRICE_CHANGED = "PRODUCT_PRICE_CHANGED";

    // ========================================================================
    // Payment Events
    // ========================================================================
    public static final String PAYMENT_INITIATED = "PAYMENT_INITIATED";
    public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String PAYMENT_REFUNDED = "PAYMENT_REFUNDED";
}
