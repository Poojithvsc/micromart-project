package com.micromart.product.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Money Value Object - Represents monetary values with currency.
 * <p>
 * PEAA Pattern: Money
 * A classic value object pattern for handling monetary calculations.
 * <p>
 * Characteristics:
 * - Immutable: All operations return new Money instances
 * - Currency-aware: Handles multi-currency scenarios
 * - Precision: Uses BigDecimal for accurate financial calculations
 * - Type-safe: Prevents mixing different currencies
 * <p>
 * JPA Annotation: @Embeddable
 * Embedded in entities rather than having its own table.
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/money.html">Money Pattern</a>
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money implements Serializable, Comparable<Money> {

    private static final long serialVersionUID = 1L;
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private CurrencyCode currency;

    /**
     * Supported currencies.
     */
    public enum CurrencyCode {
        USD, EUR, GBP, INR, JPY
    }

    private Money(BigDecimal amount, CurrencyCode currency) {
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
        this.currency = currency;
    }

    // ========================================================================
    // Factory Methods
    // ========================================================================

    /**
     * Create Money with specified amount and currency.
     */
    public static Money of(BigDecimal amount, CurrencyCode currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        return new Money(amount, currency);
    }

    /**
     * Create Money from double value.
     */
    public static Money of(double amount, CurrencyCode currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Create Money in USD.
     */
    public static Money usd(BigDecimal amount) {
        return of(amount, CurrencyCode.USD);
    }

    /**
     * Create Money in USD from double.
     */
    public static Money usd(double amount) {
        return usd(BigDecimal.valueOf(amount));
    }

    /**
     * Create zero Money in specified currency.
     */
    public static Money zero(CurrencyCode currency) {
        return of(BigDecimal.ZERO, currency);
    }

    // ========================================================================
    // Arithmetic Operations (Return new instances - Immutable)
    // ========================================================================

    /**
     * Add two Money values.
     *
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtract Money value.
     *
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiply by a factor.
     */
    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    /**
     * Multiply by integer quantity.
     */
    public Money multiply(int quantity) {
        return multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Divide by a factor.
     */
    public Money divide(BigDecimal divisor) {
        return new Money(this.amount.divide(divisor, SCALE, ROUNDING_MODE), this.currency);
    }

    /**
     * Calculate percentage of this Money.
     *
     * @param percentage Percentage as whole number (e.g., 10 for 10%)
     */
    public Money percentage(BigDecimal percentage) {
        BigDecimal factor = percentage.divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);
        return multiply(factor);
    }

    // ========================================================================
    // Comparison Methods
    // ========================================================================

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public int compareTo(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private void validateSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot operate on null Money");
        }
        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                    String.format("Currency mismatch: %s vs %s", this.currency, other.currency)
            );
        }
    }

    /**
     * Get formatted string with currency symbol.
     */
    public String format() {
        Currency javaCurrency = Currency.getInstance(currency.name());
        return String.format("%s %s", javaCurrency.getSymbol(), amount.toString());
    }

    @Override
    public String toString() {
        return String.format("%s %s", currency, amount);
    }
}
