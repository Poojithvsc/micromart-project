package com.micromart.order.domain;

import com.micromart.order.domain.valueobject.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit Tests for Order Domain Entity.
 * <p>
 * Testing Concepts Demonstrated:
 * - Testing rich domain model with state machine behavior
 * - Testing order total calculation
 * - Testing state transitions and guards
 * <p>
 * Learning Points:
 * - Order is a rich domain model with business logic
 * - State transitions follow business rules
 * - Domain invariants are enforced
 */
@DisplayName("Order Domain Entity Tests")
class OrderTest {

    private Order order;
    private Address shippingAddress;

    @BeforeEach
    void setUp() {
        shippingAddress = Address.builder()
                .street("123 Main St")
                .city("Test City")
                .state("TS")
                .zipCode("12345")
                .country("USA")
                .build();

        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .userId(1L)
                .shippingAddress(shippingAddress)
                .currency("USD")
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();
    }

    // ========================================================================
    // ADD ITEM TESTS
    // ========================================================================
    @Nested
    @DisplayName("addItem()")
    class AddItemTests {

        @Test
        @DisplayName("Should add item to order")
        void shouldAddItemToOrder() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productId(1L)
                    .productName("Test Product")
                    .quantity(2)
                    .unitPrice(new BigDecimal("50.00"))
                    .build();

            // When
            order.addItem(item);

            // Then
            assertThat(order.getItems()).hasSize(1);
            assertThat(item.getOrder()).isEqualTo(order);
        }

        @Test
        @DisplayName("Should add multiple items to order")
        void shouldAddMultipleItems() {
            // Given
            OrderItem item1 = OrderItem.builder()
                    .productId(1L)
                    .productName("Product 1")
                    .quantity(2)
                    .unitPrice(new BigDecimal("50.00"))
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .productId(2L)
                    .productName("Product 2")
                    .quantity(1)
                    .unitPrice(new BigDecimal("30.00"))
                    .build();

            // When
            order.addItem(item1);
            order.addItem(item2);

            // Then
            assertThat(order.getItems()).hasSize(2);
        }
    }

    // ========================================================================
    // TOTAL AMOUNT TESTS
    // ========================================================================
    @Nested
    @DisplayName("getTotalAmount()")
    class TotalAmountTests {

        @Test
        @DisplayName("Should calculate total amount correctly")
        void shouldCalculateTotalAmountCorrectly() {
            // Given
            OrderItem item1 = OrderItem.builder()
                    .productId(1L)
                    .productName("Product 1")
                    .quantity(2)
                    .unitPrice(new BigDecimal("50.00"))
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .productId(2L)
                    .productName("Product 2")
                    .quantity(3)
                    .unitPrice(new BigDecimal("30.00"))
                    .build();

            order.addItem(item1);
            order.addItem(item2);

            // When
            BigDecimal total = order.getTotalAmount();

            // Then - (2 * 50) + (3 * 30) = 100 + 90 = 190
            assertThat(total).isEqualByComparingTo(new BigDecimal("190.00"));
        }

        @Test
        @DisplayName("Should return zero for empty order")
        void shouldReturnZeroForEmptyOrder() {
            assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ========================================================================
    // STATE TRANSITION TESTS
    // ========================================================================
    @Nested
    @DisplayName("State Transitions")
    class StateTransitionTests {

        @Test
        @DisplayName("Should confirm pending order")
        void shouldConfirmPendingOrder() {
            // Given - order is PENDING

            // When
            order.confirm();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should mark confirmed order as payment received")
        void shouldMarkPaymentReceived() {
            // Given
            order.confirm();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            // When
            order.markPaymentReceived();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        }

        @Test
        @DisplayName("Should mark payment-completed order as shipped")
        void shouldMarkAsShipped() {
            // Given
            order.confirm();
            order.markPaymentReceived();

            // When
            order.markShipped();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(order.getShippedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should mark shipped order as delivered")
        void shouldMarkAsDelivered() {
            // Given
            order.confirm();
            order.markPaymentReceived();
            order.markShipped();

            // When
            order.markDelivered();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(order.getDeliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("Should cancel pending order")
        void shouldCancelPendingOrder() {
            // When
            order.cancel();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancelledAt()).isNotNull();
        }
    }

    // ========================================================================
    // CAN BE CANCELLED TESTS
    // ========================================================================
    @Nested
    @DisplayName("canBeCancelled()")
    class CanBeCancelledTests {

        @Test
        @DisplayName("Should return true for pending order")
        void shouldReturnTrue_ForPendingOrder() {
            assertThat(order.canBeCancelled()).isTrue();
        }

        @Test
        @DisplayName("Should return true for confirmed order")
        void shouldReturnTrue_ForConfirmedOrder() {
            order.confirm();
            assertThat(order.canBeCancelled()).isTrue();
        }

        @Test
        @DisplayName("Should return true for payment completed order")
        void shouldReturnTrue_ForPaymentCompletedOrder() {
            order.confirm();
            order.markPaymentReceived();
            assertThat(order.canBeCancelled()).isTrue();
        }

        @Test
        @DisplayName("Should return false for shipped order")
        void shouldReturnFalse_ForShippedOrder() {
            order.confirm();
            order.markPaymentReceived();
            order.markShipped();
            assertThat(order.canBeCancelled()).isFalse();
        }

        @Test
        @DisplayName("Should return false for delivered order")
        void shouldReturnFalse_ForDeliveredOrder() {
            order.confirm();
            order.markPaymentReceived();
            order.markShipped();
            order.markDelivered();
            assertThat(order.canBeCancelled()).isFalse();
        }

        @Test
        @DisplayName("Should return false for already cancelled order")
        void shouldReturnFalse_ForCancelledOrder() {
            order.cancel();
            assertThat(order.canBeCancelled()).isFalse();
        }
    }

    // ========================================================================
    // ITEM COUNT TESTS
    // ========================================================================
    @Nested
    @DisplayName("getItemCount()")
    class ItemCountTests {

        @Test
        @DisplayName("Should return total quantity of all items")
        void shouldReturnTotalQuantity() {
            // Given
            OrderItem item1 = OrderItem.builder()
                    .productId(1L)
                    .productName("Product 1")
                    .quantity(2)
                    .unitPrice(new BigDecimal("50.00"))
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .productId(2L)
                    .productName("Product 2")
                    .quantity(3)
                    .unitPrice(new BigDecimal("30.00"))
                    .build();

            order.addItem(item1);
            order.addItem(item2);

            // When
            int count = order.getItemCount();

            // Then - 2 + 3 = 5
            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("Should return zero for empty order")
        void shouldReturnZeroForEmptyOrder() {
            assertThat(order.getItemCount()).isZero();
        }
    }
}
