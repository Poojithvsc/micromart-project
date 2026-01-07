package com.micromart.common.event;

/**
 * Constants for Kafka topic names used across microservices.
 * <p>
 * Centralizing topic names ensures consistency and prevents
 * typos when producing/consuming messages.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class - prevent instantiation
    }

    /**
     * Topic for order lifecycle events.
     * Producers: order-service
     * Consumers: product-service (inventory), notification-service
     */
    public static final String ORDER_EVENTS = "order-events";

    /**
     * Topic for inventory/stock events.
     * Producers: product-service
     * Consumers: order-service
     */
    public static final String INVENTORY_EVENTS = "inventory-events";

    /**
     * Topic for payment events.
     * Producers: order-service (payment component)
     * Consumers: order-service (status update)
     */
    public static final String PAYMENT_EVENTS = "payment-events";

    /**
     * Topic for user-related events.
     * Producers: user-service
     * Consumers: order-service, notification-service
     */
    public static final String USER_EVENTS = "user-events";

    /**
     * Topic for product catalog events.
     * Producers: product-service
     * Consumers: order-service (price validation)
     */
    public static final String PRODUCT_EVENTS = "product-events";

    /**
     * Dead letter topic for failed event processing.
     * All services write here on processing failure.
     */
    public static final String DEAD_LETTER_TOPIC = "dead-letter-events";
}
