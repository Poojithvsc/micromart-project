package com.micromart.product.kafka;

import com.micromart.product.dto.request.StockReservationRequest;
import com.micromart.product.kafka.event.OrderEvent;
import com.micromart.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Order Event Consumer.
 * <p>
 * Spring Kafka: @KafkaListener
 * <p>
 * Learning Points:
 * - @KafkaListener marks methods as Kafka message handlers
 * - groupId ensures only one instance in a consumer group processes each message
 * - Manual acknowledgment for at-least-once delivery
 * - Error handling strategies (retry, DLQ)
 * <p>
 * This consumer handles order events to manage inventory:
 * - ORDER_CANCELLED: Release reserved stock
 * - ORDER_SHIPPED: Confirm reservation (deduct actual stock)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    /**
     * Consume order events from Kafka.
     * <p>
     * containerFactory: References the consumer factory
     * topics: The topic to consume from
     * groupId: Consumer group (only one consumer in group gets each message)
     */
    @KafkaListener(
            topics = "${kafka.topics.order-events:order-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("Received order event: type={}, orderNumber={}, partition={}, offset={}",
                event.getEventType(), event.getOrderNumber(), partition, offset);

        try {
            switch (event.getEventType()) {
                case OrderEvent.ORDER_CANCELLED:
                    handleOrderCancelled(event);
                    break;

                case OrderEvent.ORDER_SHIPPED:
                    handleOrderShipped(event);
                    break;

                case OrderEvent.ORDER_CREATED:
                    // Stock reservation is handled synchronously via REST API
                    log.info("Order created event received - inventory already reserved via API");
                    break;

                case OrderEvent.ORDER_CONFIRMED:
                    log.info("Order confirmed: {}", event.getOrderNumber());
                    break;

                case OrderEvent.ORDER_DELIVERED:
                    log.info("Order delivered: {}", event.getOrderNumber());
                    break;

                default:
                    log.warn("Unknown order event type: {}", event.getEventType());
            }

            // Acknowledge message after successful processing
            acknowledgment.acknowledge();
            log.debug("Acknowledged order event: {}", event.getOrderNumber());

        } catch (Exception e) {
            log.error("Error processing order event: {}", event.getOrderNumber(), e);
            // In production, implement retry or send to dead letter queue
            // For now, still acknowledge to prevent infinite retry
            acknowledgment.acknowledge();
        }
    }

    /**
     * Handle order cancellation - release reserved stock.
     */
    private void handleOrderCancelled(OrderEvent event) {
        log.info("Handling order cancellation: {}", event.getOrderNumber());

        for (OrderEvent.OrderItemEvent item : event.getItems()) {
            try {
                StockReservationRequest request = StockReservationRequest.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .orderReference(event.getOrderNumber())
                        .build();

                inventoryService.releaseStock(request);
                log.info("Released stock for product {} (qty: {}) - order: {}",
                        item.getProductId(), item.getQuantity(), event.getOrderNumber());

            } catch (Exception e) {
                log.error("Failed to release stock for product {} - order: {}",
                        item.getProductId(), event.getOrderNumber(), e);
                // Continue processing other items
            }
        }
    }

    /**
     * Handle order shipped - confirm reservation (deduct actual stock).
     */
    private void handleOrderShipped(OrderEvent event) {
        log.info("Handling order shipment: {}", event.getOrderNumber());

        for (OrderEvent.OrderItemEvent item : event.getItems()) {
            try {
                StockReservationRequest request = StockReservationRequest.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .orderReference(event.getOrderNumber())
                        .build();

                inventoryService.confirmReservation(request);
                log.info("Confirmed reservation for product {} (qty: {}) - order: {}",
                        item.getProductId(), item.getQuantity(), event.getOrderNumber());

            } catch (Exception e) {
                log.error("Failed to confirm reservation for product {} - order: {}",
                        item.getProductId(), event.getOrderNumber(), e);
                // Continue processing other items
            }
        }
    }
}
