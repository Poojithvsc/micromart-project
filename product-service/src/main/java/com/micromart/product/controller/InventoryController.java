package com.micromart.product.controller;

import com.micromart.common.dto.ApiResponse;
import com.micromart.product.dto.request.InventoryUpdateRequest;
import com.micromart.product.dto.request.StockReservationRequest;
import com.micromart.product.dto.response.InventoryResponse;
import com.micromart.product.mapper.InventoryMapper;
import com.micromart.product.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Inventory Controller - REST API for Inventory Management.
 * <p>
 * Provides endpoints for stock management, reservations, and reporting.
 * <p>
 * Learning Points:
 * - Internal API endpoints (called by order-service)
 * - Admin-only operations for stock management
 * - @PreAuthorize for method-level security
 */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "Inventory management endpoints")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    // ========================================================================
    // Public/Internal Endpoints (for inter-service calls)
    // ========================================================================

    /**
     * Get inventory for a product.
     * Used by order-service to check stock.
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory", description = "Get inventory for a product")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(@PathVariable Long productId) {
        var inventory = inventoryService.getByProductId(productId);
        var response = inventoryMapper.toResponse(inventory);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Check stock availability.
     * Used by order-service before placing orders.
     */
    @GetMapping("/check")
    @Operation(summary = "Check stock", description = "Check if product has sufficient stock")
    public ResponseEntity<ApiResponse<Boolean>> checkStock(
            @RequestParam Long productId,
            @RequestParam int quantity
    ) {
        boolean available = inventoryService.hasAvailableStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(available,
                available ? "Stock available" : "Insufficient stock"));
    }

    /**
     * Bulk check stock availability.
     * Returns list of product IDs with insufficient stock.
     */
    @PostMapping("/check-bulk")
    @Operation(summary = "Bulk check stock", description = "Check multiple products for stock availability")
    public ResponseEntity<ApiResponse<List<Long>>> checkBulkStock(
            @RequestBody List<Long> productIds,
            @RequestParam int requiredQuantity
    ) {
        var insufficientProducts = inventoryService.checkBulkAvailability(productIds, requiredQuantity);
        return ResponseEntity.ok(ApiResponse.success(insufficientProducts,
                insufficientProducts.isEmpty()
                        ? "All products have sufficient stock"
                        : "Some products have insufficient stock"));
    }

    // ========================================================================
    // Internal Endpoints (for order-service, typically secured)
    // ========================================================================

    /**
     * Reserve stock for an order.
     * Called by order-service when creating an order.
     */
    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock", description = "Reserve stock for an order (internal)")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveStock(
            @Valid @RequestBody StockReservationRequest request
    ) {
        log.info("Reserving stock: product={}, quantity={}, order={}",
                request.getProductId(), request.getQuantity(), request.getOrderReference());
        var inventory = inventoryService.reserveStock(request);
        var response = inventoryMapper.toResponse(inventory);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock reserved successfully"));
    }

    /**
     * Release reserved stock.
     * Called when order is cancelled.
     */
    @PostMapping("/release")
    @Operation(summary = "Release stock", description = "Release reserved stock (internal)")
    public ResponseEntity<ApiResponse<InventoryResponse>> releaseStock(
            @Valid @RequestBody StockReservationRequest request
    ) {
        log.info("Releasing stock: product={}, quantity={}, order={}",
                request.getProductId(), request.getQuantity(), request.getOrderReference());
        var inventory = inventoryService.releaseStock(request);
        var response = inventoryMapper.toResponse(inventory);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock released successfully"));
    }

    /**
     * Confirm reservation and deduct stock.
     * Called when order is fulfilled.
     */
    @PostMapping("/confirm")
    @Operation(summary = "Confirm reservation", description = "Confirm reservation and deduct stock (internal)")
    public ResponseEntity<ApiResponse<InventoryResponse>> confirmReservation(
            @Valid @RequestBody StockReservationRequest request
    ) {
        log.info("Confirming reservation: product={}, quantity={}, order={}",
                request.getProductId(), request.getQuantity(), request.getOrderReference());
        var inventory = inventoryService.confirmReservation(request);
        var response = inventoryMapper.toResponse(inventory);
        return ResponseEntity.ok(ApiResponse.success(response, "Reservation confirmed"));
    }

    // ========================================================================
    // Admin Endpoints
    // ========================================================================

    /**
     * Update inventory settings.
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update inventory", description = "Update inventory settings (Admin only)")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
            @Valid @RequestBody InventoryUpdateRequest request
    ) {
        log.info("Updating inventory for product {}", request.getProductId());
        var inventory = inventoryService.updateInventory(request);
        var response = inventoryMapper.toResponse(inventory);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory updated"));
    }

    /**
     * Add stock (replenishment).
     */
    @PostMapping("/product/{productId}/add")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add stock", description = "Add stock to a product (Admin only)")
    public ResponseEntity<ApiResponse<InventoryResponse>> addStock(
            @PathVariable Long productId,
            @RequestParam int quantity
    ) {
        log.info("Adding {} units to product {}", quantity, productId);
        var inventory = inventoryService.addStock(productId, quantity);
        var response = inventoryMapper.toResponse(inventory);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock added successfully"));
    }

    /**
     * Get products needing reorder.
     */
    @GetMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get reorder list", description = "Get products needing reorder (Admin only)")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getReorderList() {
        var inventories = inventoryService.getProductsNeedingReorder();
        var responses = inventories.stream()
                .map(inventoryMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get out of stock products.
     */
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get out of stock", description = "Get out of stock products (Admin only)")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getOutOfStock() {
        var inventories = inventoryService.getOutOfStockProducts();
        var responses = inventories.stream()
                .map(inventoryMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
