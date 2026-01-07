package com.micromart.product.domain;

import com.micromart.product.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit Tests for Inventory Domain Entity.
 * <p>
 * Testing Concepts Demonstrated:
 * - Testing rich domain model behavior
 * - Testing business invariants
 * - Testing state transitions
 * <p>
 * Learning Points:
 * - Domain entities should contain business logic (PEAA Domain Model)
 * - Tests verify business rules are enforced
 * - @Version field enables optimistic locking (tested via integration tests)
 */
@DisplayName("Inventory Domain Entity Tests")
class InventoryTest {

    private Inventory inventory;
    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .price(Money.of(new BigDecimal("99.99"), "USD"))
                .active(true)
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .quantity(100)
                .reservedQuantity(0)
                .reorderLevel(10)
                .reorderQuantity(50)
                .build();
    }

    // ========================================================================
    // AVAILABLE QUANTITY TESTS
    // ========================================================================
    @Nested
    @DisplayName("getAvailableQuantity()")
    class AvailableQuantityTests {

        @Test
        @DisplayName("Should return full quantity when nothing reserved")
        void shouldReturnFullQuantity_WhenNothingReserved() {
            assertThat(inventory.getAvailableQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should return quantity minus reserved")
        void shouldReturnQuantityMinusReserved() {
            // Given
            inventory.reserve(30);

            // Then
            assertThat(inventory.getAvailableQuantity()).isEqualTo(70);
        }
    }

    // ========================================================================
    // RESERVE TESTS
    // ========================================================================
    @Nested
    @DisplayName("reserve()")
    class ReserveTests {

        @Test
        @DisplayName("Should reserve stock successfully")
        void shouldReserveStock_Successfully() {
            // When
            inventory.reserve(20);

            // Then
            assertThat(inventory.getReservedQuantity()).isEqualTo(20);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(80);
            assertThat(inventory.getQuantity()).isEqualTo(100); // Total unchanged
        }

        @Test
        @DisplayName("Should reserve all available stock")
        void shouldReserveAllAvailableStock() {
            // When
            inventory.reserve(100);

            // Then
            assertThat(inventory.getReservedQuantity()).isEqualTo(100);
            assertThat(inventory.getAvailableQuantity()).isZero();
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowException_ForNegativeQuantity() {
            assertThatThrownBy(() -> inventory.reserve(-10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Should throw exception for zero quantity")
        void shouldThrowException_ForZeroQuantity() {
            assertThatThrownBy(() -> inventory.reserve(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void shouldThrowException_WhenInsufficientStock() {
            assertThatThrownBy(() -> inventory.reserve(150))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient");
        }

        @Test
        @DisplayName("Should accumulate multiple reservations")
        void shouldAccumulateMultipleReservations() {
            // When
            inventory.reserve(20);
            inventory.reserve(30);
            inventory.reserve(10);

            // Then
            assertThat(inventory.getReservedQuantity()).isEqualTo(60);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(40);
        }
    }

    // ========================================================================
    // RELEASE RESERVATION TESTS
    // ========================================================================
    @Nested
    @DisplayName("releaseReservation()")
    class ReleaseReservationTests {

        @Test
        @DisplayName("Should release reserved stock")
        void shouldReleaseReservedStock() {
            // Given
            inventory.reserve(50);

            // When
            inventory.releaseReservation(30);

            // Then
            assertThat(inventory.getReservedQuantity()).isEqualTo(20);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(80);
        }

        @Test
        @DisplayName("Should release all reserved stock")
        void shouldReleaseAllReservedStock() {
            // Given
            inventory.reserve(50);

            // When
            inventory.releaseReservation(50);

            // Then
            assertThat(inventory.getReservedQuantity()).isZero();
            assertThat(inventory.getAvailableQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowException_ForNegativeQuantity() {
            inventory.reserve(50);

            assertThatThrownBy(() -> inventory.releaseReservation(-10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when releasing more than reserved")
        void shouldThrowException_WhenReleasingMoreThanReserved() {
            inventory.reserve(50);

            assertThatThrownBy(() -> inventory.releaseReservation(60))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot release more");
        }
    }

    // ========================================================================
    // CONFIRM RESERVATION TESTS
    // ========================================================================
    @Nested
    @DisplayName("confirmReservation()")
    class ConfirmReservationTests {

        @Test
        @DisplayName("Should confirm reservation and reduce quantity")
        void shouldConfirmReservation_AndReduceQuantity() {
            // Given
            inventory.reserve(30);

            // When
            inventory.confirmReservation(30);

            // Then
            assertThat(inventory.getReservedQuantity()).isZero();
            assertThat(inventory.getQuantity()).isEqualTo(70); // Reduced from 100
            assertThat(inventory.getAvailableQuantity()).isEqualTo(70);
        }

        @Test
        @DisplayName("Should partially confirm reservation")
        void shouldPartiallyConfirmReservation() {
            // Given
            inventory.reserve(50);

            // When
            inventory.confirmReservation(30);

            // Then
            assertThat(inventory.getReservedQuantity()).isEqualTo(20);
            assertThat(inventory.getQuantity()).isEqualTo(70);
        }

        @Test
        @DisplayName("Should throw exception when confirming more than reserved")
        void shouldThrowException_WhenConfirmingMoreThanReserved() {
            inventory.reserve(30);

            assertThatThrownBy(() -> inventory.confirmReservation(50))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ========================================================================
    // ADD STOCK TESTS
    // ========================================================================
    @Nested
    @DisplayName("addStock()")
    class AddStockTests {

        @Test
        @DisplayName("Should add stock to inventory")
        void shouldAddStock() {
            // When
            inventory.addStock(50);

            // Then
            assertThat(inventory.getQuantity()).isEqualTo(150);
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowException_ForNegativeQuantity() {
            assertThatThrownBy(() -> inventory.addStock(-10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception for zero quantity")
        void shouldThrowException_ForZeroQuantity() {
            assertThatThrownBy(() -> inventory.addStock(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ========================================================================
    // REORDER TESTS
    // ========================================================================
    @Nested
    @DisplayName("needsReorder()")
    class NeedsReorderTests {

        @Test
        @DisplayName("Should return false when above reorder level")
        void shouldReturnFalse_WhenAboveReorderLevel() {
            // Given - reorder level is 10, available is 100
            assertThat(inventory.needsReorder()).isFalse();
        }

        @Test
        @DisplayName("Should return true when at reorder level")
        void shouldReturnTrue_WhenAtReorderLevel() {
            // Given - reserve until available equals reorder level
            inventory.reserve(90); // 100 - 90 = 10 = reorder level

            // Then
            assertThat(inventory.needsReorder()).isTrue();
        }

        @Test
        @DisplayName("Should return true when below reorder level")
        void shouldReturnTrue_WhenBelowReorderLevel() {
            // Given - reserve until below reorder level
            inventory.reserve(95); // 100 - 95 = 5 < 10

            // Then
            assertThat(inventory.needsReorder()).isTrue();
        }
    }

    // ========================================================================
    // OUT OF STOCK TESTS
    // ========================================================================
    @Nested
    @DisplayName("isOutOfStock()")
    class OutOfStockTests {

        @Test
        @DisplayName("Should return false when stock available")
        void shouldReturnFalse_WhenStockAvailable() {
            assertThat(inventory.isOutOfStock()).isFalse();
        }

        @Test
        @DisplayName("Should return true when all stock reserved")
        void shouldReturnTrue_WhenAllStockReserved() {
            // Given
            inventory.reserve(100);

            // Then
            assertThat(inventory.isOutOfStock()).isTrue();
        }

        @Test
        @DisplayName("Should return true when quantity is zero")
        void shouldReturnTrue_WhenQuantityIsZero() {
            // Given
            inventory.setQuantity(0);

            // Then
            assertThat(inventory.isOutOfStock()).isTrue();
        }
    }

    // ========================================================================
    // HAS STOCK TESTS
    // ========================================================================
    @Nested
    @DisplayName("hasStock()")
    class HasStockTests {

        @Test
        @DisplayName("Should return true when sufficient stock")
        void shouldReturnTrue_WhenSufficientStock() {
            assertThat(inventory.hasStock(50)).isTrue();
        }

        @Test
        @DisplayName("Should return false when insufficient stock")
        void shouldReturnFalse_WhenInsufficientStock() {
            assertThat(inventory.hasStock(150)).isFalse();
        }

        @Test
        @DisplayName("Should consider reserved stock")
        void shouldConsiderReservedStock() {
            // Given
            inventory.reserve(80);

            // Then - only 20 available
            assertThat(inventory.hasStock(20)).isTrue();
            assertThat(inventory.hasStock(21)).isFalse();
        }
    }
}
