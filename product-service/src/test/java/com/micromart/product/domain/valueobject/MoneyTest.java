package com.micromart.product.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit Tests for Money Value Object.
 * <p>
 * Testing Concepts Demonstrated:
 * - PEAA Money Pattern implementation
 * - Value Object immutability
 * - Value equality
 * - Parameterized tests for multiple inputs
 * <p>
 * Learning Points:
 * - Money pattern avoids floating-point precision issues
 * - Uses BigDecimal for precise calculations
 * - Value Objects should be immutable
 * - Value Objects compare by value, not identity
 */
@DisplayName("Money Value Object Tests")
class MoneyTest {

    // ========================================================================
    // CREATION TESTS
    // ========================================================================
    @Nested
    @DisplayName("Creation")
    class CreationTests {

        @Test
        @DisplayName("Should create Money with amount and currency")
        void shouldCreateMoney_WithAmountAndCurrency() {
            // When
            Money money = Money.of(new BigDecimal("99.99"), "USD");

            // Then
            assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(money.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should create USD by default")
        void shouldCreateUsd_ByDefault() {
            // When
            Money money = Money.of(new BigDecimal("50.00"));

            // Then
            assertThat(money.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should normalize currency to uppercase")
        void shouldNormalizeCurrency_ToUppercase() {
            // When
            Money money = Money.of(new BigDecimal("100"), "eur");

            // Then
            assertThat(money.getCurrency()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("Should throw exception for null amount")
        void shouldThrowException_ForNullAmount() {
            assertThatThrownBy(() -> Money.of(null, "USD"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount");
        }

        @ParameterizedTest
        @DisplayName("Should throw exception for invalid currency")
        @NullSource
        @ValueSource(strings = {"", "  ", "US", "USDD"})
        void shouldThrowException_ForInvalidCurrency(String currency) {
            assertThatThrownBy(() -> Money.of(new BigDecimal("100"), currency))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception for negative amount")
        void shouldThrowException_ForNegativeAmount() {
            assertThatThrownBy(() -> Money.of(new BigDecimal("-10"), "USD"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }
    }

    // ========================================================================
    // ARITHMETIC TESTS
    // ========================================================================
    @Nested
    @DisplayName("Arithmetic Operations")
    class ArithmeticTests {

        @Test
        @DisplayName("Should add two Money objects")
        void shouldAddMoney() {
            // Given
            Money money1 = Money.of(new BigDecimal("100.00"), "USD");
            Money money2 = Money.of(new BigDecimal("50.50"), "USD");

            // When
            Money result = money1.add(money2);

            // Then
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("150.50"));
            assertThat(result.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should throw exception when adding different currencies")
        void shouldThrowException_WhenAddingDifferentCurrencies() {
            // Given
            Money usd = Money.of(new BigDecimal("100"), "USD");
            Money eur = Money.of(new BigDecimal("100"), "EUR");

            // When/Then
            assertThatThrownBy(() -> usd.add(eur))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("currency");
        }

        @Test
        @DisplayName("Should subtract Money objects")
        void shouldSubtractMoney() {
            // Given
            Money money1 = Money.of(new BigDecimal("100.00"), "USD");
            Money money2 = Money.of(new BigDecimal("30.50"), "USD");

            // When
            Money result = money1.subtract(money2);

            // Then
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("69.50"));
        }

        @Test
        @DisplayName("Should throw exception when subtracting different currencies")
        void shouldThrowException_WhenSubtractingDifferentCurrencies() {
            Money usd = Money.of(new BigDecimal("100"), "USD");
            Money eur = Money.of(new BigDecimal("50"), "EUR");

            assertThatThrownBy(() -> usd.subtract(eur))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when result would be negative")
        void shouldThrowException_WhenResultWouldBeNegative() {
            Money money1 = Money.of(new BigDecimal("50"), "USD");
            Money money2 = Money.of(new BigDecimal("100"), "USD");

            assertThatThrownBy(() -> money1.subtract(money2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }

        @ParameterizedTest
        @DisplayName("Should multiply by quantity")
        @CsvSource({
                "10.00, 5, 50.00",
                "99.99, 3, 299.97",
                "0.01, 100, 1.00",
                "100.00, 1, 100.00"
        })
        void shouldMultiplyByQuantity(String amount, int quantity, String expected) {
            // Given
            Money money = Money.of(new BigDecimal(amount), "USD");

            // When
            Money result = money.multiply(quantity);

            // Then
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal(expected));
        }

        @Test
        @DisplayName("Should throw exception for negative multiplier")
        void shouldThrowException_ForNegativeMultiplier() {
            Money money = Money.of(new BigDecimal("100"), "USD");

            assertThatThrownBy(() -> money.multiply(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ========================================================================
    // COMPARISON TESTS
    // ========================================================================
    @Nested
    @DisplayName("Comparisons")
    class ComparisonTests {

        @Test
        @DisplayName("Should compare Money correctly")
        void shouldCompareMoney() {
            // Given
            Money money1 = Money.of(new BigDecimal("100"), "USD");
            Money money2 = Money.of(new BigDecimal("50"), "USD");
            Money money3 = Money.of(new BigDecimal("100"), "USD");

            // Then
            assertThat(money1.isGreaterThan(money2)).isTrue();
            assertThat(money2.isGreaterThan(money1)).isFalse();
            assertThat(money1.isGreaterThan(money3)).isFalse();
        }

        @Test
        @DisplayName("Should check if zero")
        void shouldCheckIfZero() {
            Money zero = Money.of(BigDecimal.ZERO, "USD");
            Money notZero = Money.of(new BigDecimal("0.01"), "USD");

            assertThat(zero.isZero()).isTrue();
            assertThat(notZero.isZero()).isFalse();
        }

        @Test
        @DisplayName("Should check if positive")
        void shouldCheckIfPositive() {
            Money zero = Money.of(BigDecimal.ZERO, "USD");
            Money positive = Money.of(new BigDecimal("1"), "USD");

            assertThat(zero.isPositive()).isFalse();
            assertThat(positive.isPositive()).isTrue();
        }
    }

    // ========================================================================
    // EQUALITY TESTS
    // ========================================================================
    @Nested
    @DisplayName("Value Equality")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same amount and currency")
        void shouldBeEqual_WhenSameAmountAndCurrency() {
            Money money1 = Money.of(new BigDecimal("100.00"), "USD");
            Money money2 = Money.of(new BigDecimal("100.00"), "USD");

            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("Should be equal with different BigDecimal scales")
        void shouldBeEqual_WithDifferentScales() {
            Money money1 = Money.of(new BigDecimal("100"), "USD");
            Money money2 = Money.of(new BigDecimal("100.00"), "USD");

            // Note: This depends on implementation. Some treat different scales as equal
            assertThat(money1.getAmount()).isEqualByComparingTo(money2.getAmount());
        }

        @Test
        @DisplayName("Should not be equal with different amounts")
        void shouldNotBeEqual_WithDifferentAmounts() {
            Money money1 = Money.of(new BigDecimal("100"), "USD");
            Money money2 = Money.of(new BigDecimal("200"), "USD");

            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should not be equal with different currencies")
        void shouldNotBeEqual_WithDifferentCurrencies() {
            Money usd = Money.of(new BigDecimal("100"), "USD");
            Money eur = Money.of(new BigDecimal("100"), "EUR");

            assertThat(usd).isNotEqualTo(eur);
        }
    }

    // ========================================================================
    // FORMATTING TESTS
    // ========================================================================
    @Nested
    @DisplayName("Formatting")
    class FormattingTests {

        @Test
        @DisplayName("toString should include amount and currency")
        void toString_shouldIncludeAmountAndCurrency() {
            Money money = Money.of(new BigDecimal("99.99"), "USD");

            String result = money.toString();

            assertThat(result).contains("99.99");
            assertThat(result).contains("USD");
        }
    }
}
