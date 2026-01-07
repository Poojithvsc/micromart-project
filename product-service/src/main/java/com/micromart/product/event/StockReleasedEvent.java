package com.micromart.product.event;

import lombok.Getter;

/**
 * Event published when reserved stock is released (order cancelled).
 */
@Getter
public class StockReleasedEvent extends InventoryEvent {

    private final String orderReference;

    public StockReleasedEvent(Object source, Long productId, String productSku,
                               Integer quantity, String orderReference) {
        super(source, productId, productSku, quantity);
        this.orderReference = orderReference;
    }

    @Override
    public String getEventType() {
        return "STOCK_RELEASED";
    }
}
