package com.micromart.product.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Inventory Event Listener.
 * <p>
 * Spring Concepts Demonstrated:
 * - @EventListener: Basic synchronous event handling
 * - @Async: Asynchronous event processing
 * - @TransactionalEventListener: Event handling tied to transaction phases
 * <p>
 * Learning Points:
 * - AFTER_COMMIT: Ensures event is only processed if transaction commits
 * - @Async: Processes event in separate thread (needs @EnableAsync)
 * - Useful for side effects like notifications, logging, metrics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    /**
     * Handle stock reserved event - synchronous.
     * Runs in the same transaction as the publisher.
     */
    @EventListener
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Stock reserved - Product: {} (SKU: {}), Quantity: {}, Order: {}, Remaining: {}",
                event.getProductId(),
                event.getProductSku(),
                event.getQuantity(),
                event.getOrderReference(),
                event.getRemainingStock());
    }

    /**
     * Handle stock released event - after transaction commits.
     * Only runs if the transaction successfully commits.
     * <p>
     * AFTER_COMMIT is useful when:
     * - You don't want side effects if transaction fails
     * - External API calls should only happen on success
     * - Notifications should only go out when data is persisted
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockReleased(StockReleasedEvent event) {
        log.info("Stock released (after commit) - Product: {} (SKU: {}), Quantity: {}, Order: {}",
                event.getProductId(),
                event.getProductSku(),
                event.getQuantity(),
                event.getOrderReference());
    }

    /**
     * Handle low stock event - asynchronous processing.
     * Runs in a separate thread, doesn't block the main transaction.
     * <p>
     * @Async requires @EnableAsync on a configuration class.
     * Useful for:
     * - Sending notifications/emails
     * - Triggering external integrations
     * - Heavy computations that shouldn't delay the response
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLowStock(LowStockEvent event) {
        log.warn("LOW STOCK ALERT - Product: {} (SKU: {}), Current: {}, Reorder Level: {}, Suggested Reorder: {}",
                event.getProductId(),
                event.getProductSku(),
                event.getQuantity(),
                event.getReorderLevel(),
                event.getSuggestedReorderQuantity());

        // In a real application, you might:
        // - Send email notification to warehouse
        // - Create automatic reorder request
        // - Update dashboard metrics
        // - Send Slack/Teams notification
    }

    /**
     * Handle stock replenished event.
     */
    @EventListener
    public void handleStockReplenished(StockReplenishedEvent event) {
        log.info("Stock replenished - Product: {} (SKU: {}), Added: {}, Previous: {}, New Total: {}",
                event.getProductId(),
                event.getProductSku(),
                event.getQuantity(),
                event.getPreviousQuantity(),
                event.getNewQuantity());
    }

    /**
     * Generic handler for all inventory events - for audit logging.
     * Demonstrates handling a base event type.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void auditInventoryEvent(InventoryEvent event) {
        log.debug("AUDIT - Inventory Event: {} for Product {} at {}",
                event.getEventType(),
                event.getProductId(),
                event.getOccurredAt());

        // Could persist to audit table, send to analytics, etc.
    }
}
