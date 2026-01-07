package com.micromart.order.service;

import com.micromart.common.exception.ResourceNotFoundException;
import com.micromart.order.client.InventoryClient;
import com.micromart.order.client.ProductClient;
import com.micromart.order.client.dto.ProductDto;
import com.micromart.order.client.dto.StockReservationDto;
import com.micromart.order.domain.Order;
import com.micromart.order.domain.OrderItem;
import com.micromart.order.domain.OrderStatus;
import com.micromart.order.domain.valueobject.Address;
import com.micromart.order.dto.request.AddressRequest;
import com.micromart.order.dto.request.CreateOrderRequest;
import com.micromart.order.dto.request.OrderItemRequest;
import com.micromart.order.kafka.OrderEventProducer;
import com.micromart.order.mapper.OrderMapper;
import com.micromart.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for OrderServiceImpl.
 * <p>
 * Testing Concepts Demonstrated:
 * - Testing distributed service coordination
 * - Testing Feign client integration (mocked)
 * - Testing Kafka event publishing
 * - Testing order state transitions
 * <p>
 * Learning Points:
 * - Feign clients are mocked in unit tests
 * - Kafka producer is mocked to verify events
 * - Order state machine logic is tested thoroughly
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private CreateOrderRequest createOrderRequest;
    private ProductDto testProduct;

    @BeforeEach
    void setUp() {
        Address shippingAddress = Address.builder()
                .street("123 Main St")
                .city("Test City")
                .state("TS")
                .zipCode("12345")
                .country("USA")
                .build();

        testOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .userId(1L)
                .shippingAddress(shippingAddress)
                .currency("USD")
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .build();
        testOrder.addItem(item);

        AddressRequest addressRequest = AddressRequest.builder()
                .street("123 Main St")
                .city("Test City")
                .state("TS")
                .zipCode("12345")
                .country("USA")
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .userId(1L)
                .shippingAddress(addressRequest)
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId(1L)
                                .quantity(2)
                                .build()
                ))
                .notes("Test order")
                .build();

        testProduct = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .priceAmount(new BigDecimal("99.99"))
                .active(true)
                .build();
    }

    // ========================================================================
    // GET ORDER TESTS
    // ========================================================================
    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return order when found")
        void shouldReturnOrder_WhenFound() {
            // Given
            given(orderRepository.findWithItemsById(1L)).willReturn(Optional.of(testOrder));

            // When
            Order result = orderService.getById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOrderNumber()).isEqualTo("ORD-001");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowException_WhenNotFound() {
            // Given
            given(orderRepository.findWithItemsById(999L)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> orderService.getById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order");
        }
    }

    @Nested
    @DisplayName("getByOrderNumber()")
    class GetByOrderNumberTests {

        @Test
        @DisplayName("Should return order when found by order number")
        void shouldReturnOrder_WhenFoundByOrderNumber() {
            // Given
            given(orderRepository.findWithItemsByOrderNumber("ORD-001"))
                    .willReturn(Optional.of(testOrder));

            // When
            Order result = orderService.getByOrderNumber("ORD-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    // ========================================================================
    // CREATE ORDER TESTS
    // ========================================================================
    @Nested
    @DisplayName("createOrder()")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully")
        void shouldCreateOrder_Successfully() {
            // Given
            given(productClient.getProduct(1L)).willReturn(testProduct);
            given(inventoryClient.checkStock(anyLong(), anyInt())).willReturn(true);
            given(inventoryClient.reserveStock(any(StockReservationDto.class)))
                    .willReturn(new Object());
            given(orderMapper.toAddress(any(AddressRequest.class)))
                    .willReturn(testOrder.getShippingAddress());
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                order.setOrderNumber("ORD-001");
                return order;
            });

            // When
            Order result = orderService.createOrder(createOrderRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

            verify(productClient).getProduct(1L);
            verify(inventoryClient).checkStock(1L, 2);
            verify(inventoryClient).reserveStock(any());
            verify(orderEventProducer).publishOrderCreated(any());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowException_WhenProductNotFound() {
            // Given
            given(productClient.getProduct(1L)).willReturn(null);

            // When/Then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when product inactive")
        void shouldThrowException_WhenProductInactive() {
            // Given
            testProduct.setActive(false);
            given(productClient.getProduct(1L)).willReturn(testProduct);

            // When/Then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void shouldThrowException_WhenInsufficientStock() {
            // Given
            given(productClient.getProduct(1L)).willReturn(testProduct);
            given(inventoryClient.checkStock(anyLong(), anyInt())).willReturn(false);

            // When/Then
            assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient stock");
        }
    }

    // ========================================================================
    // ORDER LIFECYCLE TESTS
    // ========================================================================
    @Nested
    @DisplayName("Order Lifecycle")
    class OrderLifecycleTests {

        @Test
        @DisplayName("Should confirm order")
        void shouldConfirmOrder() {
            // Given
            given(orderRepository.findWithItemsById(1L)).willReturn(Optional.of(testOrder));
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            Order result = orderService.confirmOrder(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            verify(orderEventProducer).publishOrderConfirmed(any());
        }

        @Test
        @DisplayName("Should cancel order when pending")
        void shouldCancelOrder_WhenPending() {
            // Given
            given(orderRepository.findWithItemsById(1L)).willReturn(Optional.of(testOrder));
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            Order result = orderService.cancelOrder(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(inventoryClient).releaseStock(any(StockReservationDto.class));
            verify(orderEventProducer).publishOrderCancelled(any());
        }

        @Test
        @DisplayName("Should throw exception when cancelling shipped order")
        void shouldThrowException_WhenCancellingShippedOrder() {
            // Given
            testOrder.setStatus(OrderStatus.SHIPPED);
            given(orderRepository.findWithItemsById(1L)).willReturn(Optional.of(testOrder));

            // When/Then
            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot be cancelled");
        }

        @Test
        @DisplayName("Should ship order when payment completed")
        void shouldShipOrder_WhenPaymentCompleted() {
            // Given
            testOrder.setStatus(OrderStatus.PAYMENT_COMPLETED);
            given(orderRepository.findWithItemsById(1L)).willReturn(Optional.of(testOrder));
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(inventoryClient.confirmReservation(any(StockReservationDto.class)))
                    .willReturn(new Object());

            // When
            Order result = orderService.shipOrder(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            verify(inventoryClient).confirmReservation(any());
            verify(orderEventProducer).publishOrderShipped(any());
        }

        @Test
        @DisplayName("Should throw exception when shipping without payment")
        void shouldThrowException_WhenShippingWithoutPayment() {
            // Given - order is still PENDING
            given(orderRepository.findWithItemsById(1L)).willReturn(Optional.of(testOrder));

            // When/Then
            assertThatThrownBy(() -> orderService.shipOrder(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("payment completed");
        }

        @Test
        @DisplayName("Should deliver order when shipped")
        void shouldDeliverOrder_WhenShipped() {
            // Given
            testOrder.setStatus(OrderStatus.SHIPPED);
            given(orderRepository.findWithItemsById(1L)).willReturn(Optional.of(testOrder));
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            Order result = orderService.deliverOrder(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            verify(orderEventProducer).publishOrderDelivered(any());
        }

        @Test
        @DisplayName("Should throw exception when delivering unshipped order")
        void shouldThrowException_WhenDeliveringUnshippedOrder() {
            // Given - order is CONFIRMED, not SHIPPED
            testOrder.setStatus(OrderStatus.CONFIRMED);
            given(orderRepository.findWithItemsById(1L)).willReturn(Optional.of(testOrder));

            // When/Then
            assertThatThrownBy(() -> orderService.deliverOrder(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("shipped before delivery");
        }
    }

    // ========================================================================
    // QUERY TESTS
    // ========================================================================
    @Nested
    @DisplayName("Query Methods")
    class QueryTests {

        @Test
        @DisplayName("Should find orders by user ID")
        void shouldFindOrdersByUserId() {
            // Given
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
            given(orderRepository.findByUserId(1L, PageRequest.of(0, 10)))
                    .willReturn(orderPage);

            // When
            Page<Order> result = orderService.findByUserId(1L, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should find orders by status")
        void shouldFindOrdersByStatus() {
            // Given
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
            given(orderRepository.findByStatus(OrderStatus.PENDING, PageRequest.of(0, 10)))
                    .willReturn(orderPage);

            // When
            Page<Order> result = orderService.findByStatus(OrderStatus.PENDING, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Should count orders by status")
        void shouldCountOrdersByStatus() {
            // Given
            given(orderRepository.countByStatus(OrderStatus.PENDING)).willReturn(5L);

            // When
            long count = orderService.countByStatus(OrderStatus.PENDING);

            // Then
            assertThat(count).isEqualTo(5L);
        }
    }
}
