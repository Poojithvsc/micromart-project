package com.micromart.product.service;

import com.micromart.product.domain.Inventory;
import com.micromart.product.dto.request.InventoryUpdateRequest;
import com.micromart.product.dto.request.StockReservationRequest;

import java.util.List;
import java.util.Optional;

/**
 * Inventory Service Interface.
 * <p>
 * PEAA Pattern: Separated Interface
 * Defines contract for inventory operations.
 */
public interface InventoryService {

    /**
     * Get or create inventory for a product.
     */
    Inventory getOrCreateInventory(Long productId);

    /**
     * Get inventory by product ID.
     */
    Inventory getByProductId(Long productId);

    /**
     * Find inventory by product ID.
     */
    Optional<Inventory> findByProductId(Long productId);

    /**
     * Update inventory settings.
     */
    Inventory updateInventory(InventoryUpdateRequest request);

    /**
     * Add stock (replenishment).
     */
    Inventory addStock(Long productId, int quantity);

    /**
     * Reserve stock for an order.
     * Uses pessimistic locking to prevent race conditions.
     */
    Inventory reserveStock(StockReservationRequest request);

    /**
     * Release reserved stock (order cancelled).
     */
    Inventory releaseStock(StockReservationRequest request);

    /**
     * Confirm reservation and deduct stock (order fulfilled).
     */
    Inventory confirmReservation(StockReservationRequest request);

    /**
     * Check if product has sufficient stock.
     */
    boolean hasAvailableStock(Long productId, int quantity);

    /**
     * Get products needing reorder.
     */
    List<Inventory> getProductsNeedingReorder();

    /**
     * Get out of stock products.
     */
    List<Inventory> getOutOfStockProducts();

    /**
     * Bulk check stock availability.
     * Returns product IDs that don't have sufficient stock.
     */
    List<Long> checkBulkAvailability(List<Long> productIds, int requiredQuantity);
}
