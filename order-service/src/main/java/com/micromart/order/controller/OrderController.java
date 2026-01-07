package com.micromart.order.controller;

import com.micromart.common.dto.ApiResponse;
import com.micromart.common.dto.PageResponse;
import com.micromart.order.domain.OrderStatus;
import com.micromart.order.dto.request.CreateOrderRequest;
import com.micromart.order.dto.response.OrderResponse;
import com.micromart.order.mapper.OrderMapper;
import com.micromart.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Order Controller - REST API for Orders.
 * <p>
 * PEAA Pattern: Remote Facade
 * Provides coarse-grained REST interface over order service.
 * <p>
 * Demonstrates:
 * - Full CRUD operations
 * - Order lifecycle management
 * - Integration with Product and Inventory services via Feign
 * - @PreAuthorize for method-level security
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    /**
     * Create a new order.
     */
    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        log.info("Creating order for user {}", request.getUserId());
        var order = orderService.createOrder(request);
        var response = orderMapper.toResponse(order);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order created successfully"));
    }

    /**
     * Get order by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order", description = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        var order = orderService.getById(id);
        var response = orderMapper.toResponse(order);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get order by order number.
     */
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Get order by order number")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(@PathVariable String orderNumber) {
        var order = orderService.getByOrderNumber(orderNumber);
        var response = orderMapper.toResponse(order);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all orders with pagination (Admin).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all orders", description = "Get all orders with pagination (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var page = orderService.findAll(pageable);
        var responses = page.map(orderMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    /**
     * Get orders for a specific user.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user orders", description = "Get orders for a specific user")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getUserOrders(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var page = orderService.findByUserId(userId, pageable);
        var responses = page.map(orderMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    /**
     * Get orders by status (Admin).
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by status", description = "Get orders by status (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var page = orderService.findByStatus(status, pageable);
        var responses = page.map(orderMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    /**
     * Get user's orders by status.
     */
    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Get user orders by status", description = "Get user's orders by status")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrdersByStatus(
            @PathVariable Long userId,
            @PathVariable OrderStatus status
    ) {
        var orders = orderService.findByUserIdAndStatus(userId, status);
        var responses = orderMapper.toResponseList(orders);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ========================================================================
    // Order Lifecycle Operations
    // ========================================================================

    /**
     * Confirm an order (typically after payment).
     */
    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm order", description = "Confirm an order after payment")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(@PathVariable Long id) {
        log.info("Confirming order {}", id);
        var order = orderService.confirmOrder(id);
        var response = orderMapper.toResponse(order);
        return ResponseEntity.ok(ApiResponse.success(response, "Order confirmed"));
    }

    /**
     * Cancel an order.
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        log.info("Cancelling order {}", id);
        var order = orderService.cancelOrder(id);
        var response = orderMapper.toResponse(order);
        return ResponseEntity.ok(ApiResponse.success(response, "Order cancelled"));
    }

    /**
     * Mark order as shipped (Admin).
     */
    @PostMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ship order", description = "Mark order as shipped (Admin only)")
    public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(@PathVariable Long id) {
        log.info("Shipping order {}", id);
        var order = orderService.shipOrder(id);
        var response = orderMapper.toResponse(order);
        return ResponseEntity.ok(ApiResponse.success(response, "Order shipped"));
    }

    /**
     * Mark order as delivered (Admin).
     */
    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deliver order", description = "Mark order as delivered (Admin only)")
    public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(@PathVariable Long id) {
        log.info("Delivering order {}", id);
        var order = orderService.deliverOrder(id);
        var response = orderMapper.toResponse(order);
        return ResponseEntity.ok(ApiResponse.success(response, "Order delivered"));
    }

    // ========================================================================
    // Statistics Endpoints
    // ========================================================================

    /**
     * Get order count by status (Admin).
     */
    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Count by status", description = "Get order count by status (Admin only)")
    public ResponseEntity<ApiResponse<Long>> countByStatus(@PathVariable OrderStatus status) {
        long count = orderService.countByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Get user's order count.
     */
    @GetMapping("/count/user/{userId}")
    @Operation(summary = "Count user orders", description = "Get user's order count")
    public ResponseEntity<ApiResponse<Long>> countByUserId(@PathVariable Long userId) {
        long count = orderService.countByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
