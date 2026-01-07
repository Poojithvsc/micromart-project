package com.micromart.product.event;

import lombok.Getter;

/**
 * Event published when stock is reserved for an order.
 */
@Getter
public class StockReservedEvent extends InventoryEvent {

    private final String orderReference;
    private final Integer remainingStock;

    public StockReservedEvent(Object source, Long productId, String productSku,
                               Integer quantity, String orderReference, Integer remainingStock) {
        super(source, productId, productSku, quantity);
        this.orderReference = orderReference;
        this.remainingStock = remainingStock;
    }

    @Override
    public String getEventType() {
        return "STOCK_RESERVED";
    }
}
