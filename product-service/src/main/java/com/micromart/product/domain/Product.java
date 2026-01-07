package com.micromart.product.domain;

import com.micromart.common.domain.AuditableEntity;
import com.micromart.product.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.*;

/**
 * Product Entity - Domain Model with Money Value Object.
 * <p>
 * PEAA Patterns:
 * - Domain Model: Rich entity with behavior
 * - Money: Embedded Money value object for price
 * - Embedded Value: @Embedded for value objects
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_sku", columnList = "sku", unique = true),
        @Index(name = "idx_product_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends AuditableEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    /**
     * Price using Money value object.
     * Demonstrates @Embedded value object pattern.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private Money price;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    // ========================================================================
    // Domain Behavior
    // ========================================================================

    public void updatePrice(Money newPrice) {
        if (newPrice == null || newPrice.isNegative()) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = newPrice;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
