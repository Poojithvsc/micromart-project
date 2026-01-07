package com.micromart.product.service;

import com.micromart.common.exception.ResourceNotFoundException;
import com.micromart.product.domain.Inventory;
import com.micromart.product.domain.Product;
import com.micromart.product.dto.request.InventoryUpdateRequest;
import com.micromart.product.dto.request.StockReservationRequest;
import com.micromart.product.event.InventoryEventPublisher;
import com.micromart.product.repository.InventoryRepository;
import com.micromart.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Inventory Service Implementation.
 * <p>
 * PEAA Patterns:
 * - Service Layer: Business logic encapsulation
 * - Unit of Work: @Transactional manages transactions
 * - Domain Events: Publishes events for inventory changes
 * <p>
 * Learning Points:
 * - Pessimistic locking for concurrent stock updates
 * - Event publishing for decoupled notifications
 * - Optimistic locking via @Version in entity
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryEventPublisher eventPublisher;

    @Override
    @Transactional
    public Inventory getOrCreateInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseGet(() -> createInventoryForProduct(productId));
    }

    @Override
    public Inventory getByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
    }

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public Inventory updateInventory(InventoryUpdateRequest request) {
        log.info("Updating inventory for product {}", request.getProductId());

        Inventory inventory = getOrCreateInventory(request.getProductId());

        if (request.getQuantity() != null) {
            inventory.setQuantity(request.getQuantity());
        }
        if (request.getReorderLevel() != null) {
            inventory.setReorderLevel(request.getReorderLevel());
        }
        if (request.getReorderQuantity() != null) {
            inventory.setReorderQuantity(request.getReorderQuantity());
        }

        Inventory saved = inventoryRepository.save(inventory);

        // Check if reorder is needed
        if (saved.needsReorder()) {
            eventPublisher.publishLowStock(saved);
        }

        return saved;
    }

    @Override
    @Transactional
    public Inventory addStock(Long productId, int quantity) {
        log.info("Adding {} units to product {}", quantity, productId);

        // Use pessimistic lock for concurrent updates
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseGet(() -> createInventoryForProduct(productId));

        int previousQuantity = inventory.getQuantity();
        inventory.addStock(quantity);

        Inventory saved = inventoryRepository.save(inventory);

        // Publish replenishment event
        eventPublisher.publishStockReplenished(saved, quantity, previousQuantity);

        return saved;
    }

    /**
     * Reserve stock with pessimistic locking.
     * <p>
     * IMPORTANT: Uses findByProductIdForUpdate which applies PESSIMISTIC_WRITE lock.
     * This prevents race conditions when multiple orders try to reserve same product.
     */
    @Override
    @Transactional
    public Inventory reserveStock(StockReservationRequest request) {
        log.info("Reserving {} units of product {} for order {}",
                request.getQuantity(), request.getProductId(), request.getOrderReference());

        // PESSIMISTIC_WRITE lock prevents concurrent modifications
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", request.getProductId()));

        // Business logic in domain entity
        inventory.reserve(request.getQuantity());

        Inventory saved = inventoryRepository.save(inventory);

        // Publish event for other components
        eventPublisher.publishStockReserved(saved, request.getQuantity(), request.getOrderReference());

        // Check if low stock alert needed
        if (saved.needsReorder()) {
            eventPublisher.publishLowStock(saved);
        }

        return saved;
    }

    @Override
    @Transactional
    public Inventory releaseStock(StockReservationRequest request) {
        log.info("Releasing {} units of product {} for order {}",
                request.getQuantity(), request.getProductId(), request.getOrderReference());

        Inventory inventory = inventoryRepository.findByProductIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", request.getProductId()));

        inventory.releaseReservation(request.getQuantity());

        Inventory saved = inventoryRepository.save(inventory);

        eventPublisher.publishStockReleased(saved, request.getQuantity(), request.getOrderReference());

        return saved;
    }

    @Override
    @Transactional
    public Inventory confirmReservation(StockReservationRequest request) {
        log.info("Confirming reservation of {} units of product {} for order {}",
                request.getQuantity(), request.getProductId(), request.getOrderReference());

        Inventory inventory = inventoryRepository.findByProductIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", request.getProductId()));

        inventory.confirmReservation(request.getQuantity());

        Inventory saved = inventoryRepository.save(inventory);

        // Check if reorder needed after confirmation
        if (saved.needsReorder()) {
            eventPublisher.publishLowStock(saved);
        }

        return saved;
    }

    @Override
    public boolean hasAvailableStock(Long productId, int quantity) {
        return inventoryRepository.hasAvailableStock(productId, quantity);
    }

    @Override
    public List<Inventory> getProductsNeedingReorder() {
        return inventoryRepository.findNeedingReorder();
    }

    @Override
    public List<Inventory> getOutOfStockProducts() {
        return inventoryRepository.findOutOfStock();
    }

    @Override
    public List<Long> checkBulkAvailability(List<Long> productIds, int requiredQuantity) {
        return inventoryRepository.findInsufficientStockProducts(productIds, requiredQuantity);
    }

    /**
     * Create initial inventory for a product.
     */
    private Inventory createInventoryForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(0)
                .reservedQuantity(0)
                .reorderLevel(10)
                .reorderQuantity(50)
                .build();

        return inventoryRepository.save(inventory);
    }
}
