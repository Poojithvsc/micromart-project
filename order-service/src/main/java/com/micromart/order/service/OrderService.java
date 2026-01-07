package com.micromart.order.service;

import com.micromart.order.domain.Order;
import com.micromart.order.domain.OrderStatus;
import com.micromart.order.dto.request.CreateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Order Service Interface.
 * <p>
 * PEAA Pattern: Separated Interface
 * Defines contract for order operations.
 */
public interface OrderService {

    /**
     * Create a new order.
     * Validates stock, reserves inventory, and creates order.
     */
    Order createOrder(CreateOrderRequest request);

    /**
     * Get order by ID.
     */
    Order getById(Long id);

    /**
     * Find order by ID.
     */
    Optional<Order> findById(Long id);

    /**
     * Get order by order number.
     */
    Order getByOrderNumber(String orderNumber);

    /**
     * Find order by order number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Get all orders with pagination.
     */
    Page<Order> findAll(Pageable pageable);

    /**
     * Get orders by user ID.
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * Get orders by status.
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Get user's orders by status.
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Confirm an order (after payment).
     */
    Order confirmOrder(Long orderId);

    /**
     * Cancel an order.
     * Releases reserved inventory.
     */
    Order cancelOrder(Long orderId);

    /**
     * Mark order as shipped.
     */
    Order shipOrder(Long orderId);

    /**
     * Mark order as delivered.
     */
    Order deliverOrder(Long orderId);

    /**
     * Get order count by status.
     */
    long countByStatus(OrderStatus status);

    /**
     * Get user's order count.
     */
    long countByUserId(Long userId);
}
