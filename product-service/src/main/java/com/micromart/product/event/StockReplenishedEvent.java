package com.micromart.product.event;

import lombok.Getter;

/**
 * Event published when stock is replenished.
 */
@Getter
public class StockReplenishedEvent extends InventoryEvent {

    private final Integer previousQuantity;
    private final Integer newQuantity;

    public StockReplenishedEvent(Object source, Long productId, String productSku,
                                  Integer addedQuantity, Integer previousQuantity, Integer newQuantity) {
        super(source, productId, productSku, addedQuantity);
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
    }

    @Override
    public String getEventType() {
        return "STOCK_REPLENISHED";
    }
}
