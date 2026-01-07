package com.micromart.order.kafka;

import com.micromart.order.domain.Order;
import com.micromart.order.kafka.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Order Event Producer.
 * <p>
 * Spring Kafka: KafkaTemplate
 * <p>
 * Learning Points:
 * - KafkaTemplate is the main interface for sending messages
 * - Use send() for async, sendDefault() for default topic
 * - Handle success/failure callbacks for reliability
 * - @Value injects topic name from configuration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topics.order-events:order-events}")
    private String orderEventsTopic;

    /**
     * Publish order created event.
     */
    public void publishOrderCreated(Order order) {
        OrderEvent event = createEvent(order, OrderEvent.ORDER_CREATED);
        sendEvent(event);
    }

    /**
     * Publish order confirmed event.
     */
    public void publishOrderConfirmed(Order order) {
        OrderEvent event = createEvent(order, OrderEvent.ORDER_CONFIRMED);
        sendEvent(event);
    }

    /**
     * Publish order cancelled event.
     */
    public void publishOrderCancelled(Order order) {
        OrderEvent event = createEvent(order, OrderEvent.ORDER_CANCELLED);
        sendEvent(event);
    }

    /**
     * Publish order shipped event.
     */
    public void publishOrderShipped(Order order) {
        OrderEvent event = createEvent(order, OrderEvent.ORDER_SHIPPED);
        sendEvent(event);
    }

    /**
     * Publish order delivered event.
     */
    public void publishOrderDelivered(Order order) {
        OrderEvent event = createEvent(order, OrderEvent.ORDER_DELIVERED);
        sendEvent(event);
    }

    /**
     * Send event to Kafka.
     * Uses order number as key for partition consistency.
     */
    private void sendEvent(OrderEvent event) {
        log.info("Publishing order event: type={}, orderNumber={}",
                event.getEventType(), event.getOrderNumber());

        // Use order number as key - ensures all events for same order go to same partition
        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(orderEventsTopic, event.getOrderNumber(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order event sent successfully: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send order event: {}", event.getOrderNumber(), ex);
                // In production, implement retry logic or dead letter queue
            }
        });
    }

    /**
     * Create OrderEvent from Order entity.
     */
    private OrderEvent createEvent(Order order, String eventType) {
        return OrderEvent.builder()
                .eventType(eventType)
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .items(order.getItems().stream()
                        .map(item -> OrderEvent.OrderItemEvent.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .build())
                        .collect(Collectors.toList()))
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
