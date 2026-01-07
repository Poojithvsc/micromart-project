package com.micromart.product.service;

import com.micromart.common.exception.ResourceNotFoundException;
import com.micromart.product.domain.Inventory;
import com.micromart.product.domain.Product;
import com.micromart.product.domain.valueobject.Money;
import com.micromart.product.dto.request.InventoryUpdateRequest;
import com.micromart.product.dto.request.StockReservationRequest;
import com.micromart.product.event.InventoryEventPublisher;
import com.micromart.product.repository.InventoryRepository;
import com.micromart.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for InventoryServiceImpl.
 * <p>
 * Testing Concepts Demonstrated:
 * - Testing stock reservation logic
 * - Testing pessimistic locking scenarios
 * - Testing event publishing
 * - Testing business rules (e.g., low stock alerts)
 * <p>
 * Learning Points:
 * - Domain logic is tested through service layer
 * - Events are verified using Mockito verify()
 * - ArgumentCaptor captures values for assertion
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl Unit Tests")
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product testProduct;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .price(Money.of(new BigDecimal("99.99"), "USD"))
                .active(true)
                .build();

        testInventory = Inventory.builder()
                .id(1L)
                .product(testProduct)
                .quantity(100)
                .reservedQuantity(0)
                .reorderLevel(10)
                .reorderQuantity(50)
                .build();
    }

    // ========================================================================
    // GET INVENTORY TESTS
    // ========================================================================
    @Nested
    @DisplayName("getByProductId()")
    class GetByProductIdTests {

        @Test
        @DisplayName("Should return inventory when found")
        void shouldReturnInventory_WhenFound() {
            // Given
            given(inventoryRepository.findByProductId(1L)).willReturn(Optional.of(testInventory));

            // When
            Inventory result = inventoryService.getByProductId(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowException_WhenNotFound() {
            // Given
            given(inventoryRepository.findByProductId(999L)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> inventoryService.getByProductId(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inventory");
        }
    }

    // ========================================================================
    // RESERVE STOCK TESTS
    // ========================================================================
    @Nested
    @DisplayName("reserveStock()")
    class ReserveStockTests {

        @Test
        @DisplayName("Should reserve stock successfully")
        void shouldReserveStock_Successfully() {
            // Given
            StockReservationRequest request = StockReservationRequest.builder()
                    .productId(1L)
                    .quantity(10)
                    .orderReference("ORD-001")
                    .build();

            given(inventoryRepository.findByProductIdForUpdate(1L))
                    .willReturn(Optional.of(testInventory));
            given(inventoryRepository.save(any(Inventory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            Inventory result = inventoryService.reserveStock(request);

            // Then
            assertThat(result.getReservedQuantity()).isEqualTo(10);
            assertThat(result.getAvailableQuantity()).isEqualTo(90);

            verify(eventPublisher).publishStockReserved(any(), eq(10), eq("ORD-001"));
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void shouldThrowException_WhenInsufficientStock() {
            // Given
            StockReservationRequest request = StockReservationRequest.builder()
                    .productId(1L)
                    .quantity(150) // More than available
                    .orderReference("ORD-001")
                    .build();

            given(inventoryRepository.findByProductIdForUpdate(1L))
                    .willReturn(Optional.of(testInventory));

            // When/Then
            assertThatThrownBy(() -> inventoryService.reserveStock(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient");

            verify(eventPublisher, never()).publishStockReserved(any(), anyInt(), any());
        }

        @Test
        @DisplayName("Should publish low stock event when below reorder level")
        void shouldPublishLowStockEvent_WhenBelowReorderLevel() {
            // Given - inventory with 15 units, reorder level 10
            testInventory.setQuantity(15);
            testInventory.setReorderLevel(10);

            StockReservationRequest request = StockReservationRequest.builder()
                    .productId(1L)
                    .quantity(10) // Will leave 5 available (below reorder level)
                    .orderReference("ORD-001")
                    .build();

            given(inventoryRepository.findByProductIdForUpdate(1L))
                    .willReturn(Optional.of(testInventory));
            given(inventoryRepository.save(any(Inventory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            inventoryService.reserveStock(request);

            // Then
            verify(eventPublisher).publishLowStock(any(Inventory.class));
        }

        @Test
        @DisplayName("Should throw exception when inventory not found")
        void shouldThrowException_WhenInventoryNotFound() {
            // Given
            StockReservationRequest request = StockReservationRequest.builder()
                    .productId(999L)
                    .quantity(10)
                    .orderReference("ORD-001")
                    .build();

            given(inventoryRepository.findByProductIdForUpdate(999L))
                    .willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> inventoryService.reserveStock(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========================================================================
    // RELEASE STOCK TESTS
    // ========================================================================
    @Nested
    @DisplayName("releaseStock()")
    class ReleaseStockTests {

        @Test
        @DisplayName("Should release reserved stock")
        void shouldReleaseReservedStock() {
            // Given - inventory with some reserved stock
            testInventory.reserve(20);
            assertThat(testInventory.getReservedQuantity()).isEqualTo(20);

            StockReservationRequest request = StockReservationRequest.builder()
                    .productId(1L)
                    .quantity(20)
                    .orderReference("ORD-001")
                    .build();

            given(inventoryRepository.findByProductIdForUpdate(1L))
                    .willReturn(Optional.of(testInventory));
            given(inventoryRepository.save(any(Inventory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            Inventory result = inventoryService.releaseStock(request);

            // Then
            assertThat(result.getReservedQuantity()).isZero();
            assertThat(result.getAvailableQuantity()).isEqualTo(100);

            verify(eventPublisher).publishStockReleased(any(), eq(20), eq("ORD-001"));
        }
    }

    // ========================================================================
    // CONFIRM RESERVATION TESTS
    // ========================================================================
    @Nested
    @DisplayName("confirmReservation()")
    class ConfirmReservationTests {

        @Test
        @DisplayName("Should confirm reservation and reduce actual quantity")
        void shouldConfirmReservation() {
            // Given - inventory with reserved stock
            testInventory.reserve(20);
            assertThat(testInventory.getReservedQuantity()).isEqualTo(20);
            assertThat(testInventory.getQuantity()).isEqualTo(100);

            StockReservationRequest request = StockReservationRequest.builder()
                    .productId(1L)
                    .quantity(20)
                    .orderReference("ORD-001")
                    .build();

            given(inventoryRepository.findByProductIdForUpdate(1L))
                    .willReturn(Optional.of(testInventory));
            given(inventoryRepository.save(any(Inventory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            Inventory result = inventoryService.confirmReservation(request);

            // Then
            assertThat(result.getReservedQuantity()).isZero();
            assertThat(result.getQuantity()).isEqualTo(80); // Reduced by confirmed amount
        }
    }

    // ========================================================================
    // ADD STOCK TESTS
    // ========================================================================
    @Nested
    @DisplayName("addStock()")
    class AddStockTests {

        @Test
        @DisplayName("Should add stock to existing inventory")
        void shouldAddStock_ToExistingInventory() {
            // Given
            given(inventoryRepository.findByProductIdForUpdate(1L))
                    .willReturn(Optional.of(testInventory));
            given(inventoryRepository.save(any(Inventory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            Inventory result = inventoryService.addStock(1L, 50);

            // Then
            assertThat(result.getQuantity()).isEqualTo(150);
            verify(eventPublisher).publishStockReplenished(any(), eq(50), eq(100));
        }

        @Test
        @DisplayName("Should create inventory if not exists")
        void shouldCreateInventory_IfNotExists() {
            // Given
            given(inventoryRepository.findByProductIdForUpdate(1L))
                    .willReturn(Optional.empty());
            given(productRepository.findById(1L))
                    .willReturn(Optional.of(testProduct));
            given(inventoryRepository.save(any(Inventory.class)))
                    .willAnswer(invocation -> {
                        Inventory inv = invocation.getArgument(0);
                        inv.setId(1L);
                        return inv;
                    });

            // When
            Inventory result = inventoryService.addStock(1L, 50);

            // Then
            assertThat(result.getQuantity()).isEqualTo(50);
            verify(inventoryRepository, times(2)).save(any(Inventory.class));
        }
    }

    // ========================================================================
    // AVAILABILITY TESTS
    // ========================================================================
    @Nested
    @DisplayName("hasAvailableStock()")
    class AvailabilityTests {

        @Test
        @DisplayName("Should return true when stock is available")
        void shouldReturnTrue_WhenStockAvailable() {
            // Given
            given(inventoryRepository.hasAvailableStock(1L, 50)).willReturn(true);

            // When
            boolean result = inventoryService.hasAvailableStock(1L, 50);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when stock is insufficient")
        void shouldReturnFalse_WhenStockInsufficient() {
            // Given
            given(inventoryRepository.hasAvailableStock(1L, 200)).willReturn(false);

            // When
            boolean result = inventoryService.hasAvailableStock(1L, 200);

            // Then
            assertThat(result).isFalse();
        }
    }

    // ========================================================================
    // UPDATE INVENTORY TESTS
    // ========================================================================
    @Nested
    @DisplayName("updateInventory()")
    class UpdateInventoryTests {

        @Test
        @DisplayName("Should update inventory fields")
        void shouldUpdateInventoryFields() {
            // Given
            InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                    .productId(1L)
                    .quantity(200)
                    .reorderLevel(20)
                    .reorderQuantity(100)
                    .build();

            given(inventoryRepository.findByProductId(1L))
                    .willReturn(Optional.of(testInventory));
            given(inventoryRepository.save(any(Inventory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            Inventory result = inventoryService.updateInventory(request);

            // Then
            assertThat(result.getQuantity()).isEqualTo(200);
            assertThat(result.getReorderLevel()).isEqualTo(20);
            assertThat(result.getReorderQuantity()).isEqualTo(100);
        }
    }
}
