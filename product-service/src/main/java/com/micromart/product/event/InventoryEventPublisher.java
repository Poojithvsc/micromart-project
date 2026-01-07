package com.micromart.product.event;

import com.micromart.product.domain.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Inventory Event Publisher.
 * <p>
 * Encapsulates event creation and publishing logic.
 * Decouples service layer from Spring's ApplicationEventPublisher.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishStockReserved(Inventory inventory, int quantity, String orderReference) {
        StockReservedEvent event = new StockReservedEvent(
                this,
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                quantity,
                orderReference,
                inventory.getAvailableQuantity()
        );
        log.debug("Publishing StockReservedEvent for product {}", inventory.getProduct().getId());
        eventPublisher.publishEvent(event);
    }

    public void publishStockReleased(Inventory inventory, int quantity, String orderReference) {
        StockReleasedEvent event = new StockReleasedEvent(
                this,
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                quantity,
                orderReference
        );
        log.debug("Publishing StockReleasedEvent for product {}", inventory.getProduct().getId());
        eventPublisher.publishEvent(event);
    }

    public void publishLowStock(Inventory inventory) {
        LowStockEvent event = new LowStockEvent(
                this,
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                inventory.getQuantity(),
                inventory.getReorderLevel(),
                inventory.getReorderQuantity()
        );
        log.debug("Publishing LowStockEvent for product {}", inventory.getProduct().getId());
        eventPublisher.publishEvent(event);
    }

    public void publishStockReplenished(Inventory inventory, int addedQuantity, int previousQuantity) {
        StockReplenishedEvent event = new StockReplenishedEvent(
                this,
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                addedQuantity,
                previousQuantity,
                inventory.getQuantity()
        );
        log.debug("Publishing StockReplenishedEvent for product {}", inventory.getProduct().getId());
        eventPublisher.publishEvent(event);
    }
}
