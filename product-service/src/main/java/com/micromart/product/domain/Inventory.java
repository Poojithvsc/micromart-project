package com.micromart.product.domain;

import com.micromart.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Inventory Entity - Stock management for products.
 * <p>
 * PEAA Patterns:
 * - Domain Model: Rich entity with business behavior
 * - Optimistic Locking: @Version for concurrent updates
 * <p>
 * Learning Points:
 * - @Version enables optimistic locking to prevent lost updates
 * - Business methods encapsulate domain logic
 * - Throws domain-specific exceptions for invalid operations
 */
@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Inventory extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "reorder_level")
    @Builder.Default
    private Integer reorderLevel = 10;

    @Column(name = "reorder_quantity")
    @Builder.Default
    private Integer reorderQuantity = 50;

    /**
     * Optimistic Locking Version.
     * Prevents lost updates when multiple transactions modify the same inventory.
     * JPA automatically increments this on each update.
     */
    @Version
    private Long version;

    // ========================================================================
    // Domain Behavior (Rich Domain Model)
    // ========================================================================

    /**
     * Get available quantity (total - reserved).
     */
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    /**
     * Check if product is in stock.
     */
    public boolean isInStock() {
        return getAvailableQuantity() > 0;
    }

    /**
     * Check if reorder is needed.
     */
    public boolean needsReorder() {
        return quantity <= reorderLevel;
    }

    /**
     * Reserve stock for an order.
     * Demonstrates encapsulated business logic.
     *
     * @param amount quantity to reserve
     * @throws IllegalStateException if insufficient stock
     */
    public void reserve(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Reserve amount must be positive");
        }
        if (getAvailableQuantity() < amount) {
            throw new IllegalStateException(
                    String.format("Insufficient stock for product %d. Available: %d, Requested: %d",
                            product.getId(), getAvailableQuantity(), amount));
        }
        this.reservedQuantity += amount;
    }

    /**
     * Release reserved stock (e.g., order cancelled).
     *
     * @param amount quantity to release
     */
    public void releaseReservation(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Release amount must be positive");
        }
        if (amount > reservedQuantity) {
            throw new IllegalStateException(
                    String.format("Cannot release %d units. Only %d reserved.", amount, reservedQuantity));
        }
        this.reservedQuantity -= amount;
    }

    /**
     * Confirm reservation and reduce actual stock (order fulfilled).
     *
     * @param amount quantity to deduct
     */
    public void confirmReservation(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Confirm amount must be positive");
        }
        if (amount > reservedQuantity) {
            throw new IllegalStateException(
                    String.format("Cannot confirm %d units. Only %d reserved.", amount, reservedQuantity));
        }
        this.reservedQuantity -= amount;
        this.quantity -= amount;
    }

    /**
     * Add stock (restock operation).
     *
     * @param amount quantity to add
     */
    public void addStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Add amount must be positive");
        }
        this.quantity += amount;
    }

    /**
     * Remove stock directly (damaged, expired, etc.).
     *
     * @param amount quantity to remove
     */
    public void removeStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Remove amount must be positive");
        }
        if (amount > getAvailableQuantity()) {
            throw new IllegalStateException(
                    String.format("Cannot remove %d units. Only %d available.", amount, getAvailableQuantity()));
        }
        this.quantity -= amount;
    }

    /**
     * Check if requested quantity can be reserved.
     */
    public boolean canReserve(int amount) {
        return getAvailableQuantity() >= amount;
    }

    /**
     * Check if the inventory has at least the specified quantity in stock.
     *
     * @param amount quantity to check
     * @return true if available quantity is at least the specified amount
     */
    public boolean hasStock(int amount) {
        return getAvailableQuantity() >= amount;
    }

    /**
     * Check if the inventory is out of stock.
     *
     * @return true if available quantity is zero or less
     */
    public boolean isOutOfStock() {
        return getAvailableQuantity() <= 0;
    }
}
