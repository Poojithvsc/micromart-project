package com.micromart.product.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Base Inventory Event.
 * <p>
 * PEAA Pattern: Domain Event
 * Allows decoupled communication within the service.
 * <p>
 * Spring Concept: ApplicationEvent
 * Base class for Spring's event system.
 */
@Getter
public abstract class InventoryEvent extends ApplicationEvent {

    private final Long productId;
    private final String productSku;
    private final Integer quantity;
    private final LocalDateTime occurredAt;

    protected InventoryEvent(Object source, Long productId, String productSku, Integer quantity) {
        super(source);
        this.productId = productId;
        this.productSku = productSku;
        this.quantity = quantity;
        this.occurredAt = LocalDateTime.now();
    }

    public abstract String getEventType();
}
