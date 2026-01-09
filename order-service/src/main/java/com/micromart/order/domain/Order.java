package com.micromart.order.domain;

import com.micromart.common.domain.AuditableEntity;
import com.micromart.order.domain.valueobject.Address;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order Entity - Domain Model with embedded Address Value Object.
 * <p>
 * PEAA Patterns:
 * - Domain Model: Rich entity with behavior and lifecycle
 * - Value Object: Embedded Address for shipping
 * - Entity relationships: @OneToMany with OrderItems
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "order_number", unique = true),
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends AuditableEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Shipping address using embedded Value Object.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "shipping_street")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))
    })
    private Address shippingAddress;

    @Column(name = "total_amount", precision = 19, scale = 2)
    @Getter(AccessLevel.NONE)
    private BigDecimal totalAmount;

    /**
     * Get the total amount of the order.
     * Returns BigDecimal.ZERO if the order is empty or total hasn't been calculated.
     */
    public BigDecimal getTotalAmount() {
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ========================================================================
    // Domain Behavior - Rich Domain Model
    // ========================================================================

    @PrePersist
    public void prePersist() {
        if (this.orderNumber == null) {
            this.orderNumber = generateOrderNumber();
        }
        if (this.orderedAt == null) {
            this.orderedAt = LocalDateTime.now();
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order can only be confirmed from PENDING status");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void markPaymentCompleted() {
        this.status = OrderStatus.PAYMENT_COMPLETED;
    }

    public void markShipped() {
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }

    public void markDelivered() {
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel shipped or delivered order");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public boolean canBeCancelled() {
        return this.status != OrderStatus.SHIPPED &&
               this.status != OrderStatus.DELIVERED &&
               this.status != OrderStatus.CANCELLED;
    }

    /**
     * Mark payment as received for this order.
     * Alias for markPaymentCompleted() for API consistency.
     */
    public void markPaymentReceived() {
        markPaymentCompleted();
    }

    /**
     * Get the total number of items in this order.
     *
     * @return sum of quantities across all order items
     */
    public int getItemCount() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
}
