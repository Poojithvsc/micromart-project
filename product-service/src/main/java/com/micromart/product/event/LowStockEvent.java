package com.micromart.product.event;

import lombok.Getter;

/**
 * Event published when stock falls below reorder level.
 * Can trigger automated reordering or notifications.
 */
@Getter
public class LowStockEvent extends InventoryEvent {

    private final Integer reorderLevel;
    private final Integer suggestedReorderQuantity;

    public LowStockEvent(Object source, Long productId, String productSku,
                          Integer currentQuantity, Integer reorderLevel, Integer suggestedReorderQuantity) {
        super(source, productId, productSku, currentQuantity);
        this.reorderLevel = reorderLevel;
        this.suggestedReorderQuantity = suggestedReorderQuantity;
    }

    @Override
    public String getEventType() {
        return "LOW_STOCK";
    }
}
