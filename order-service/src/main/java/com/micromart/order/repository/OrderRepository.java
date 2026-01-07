package com.micromart.order.repository;

import com.micromart.order.domain.Order;
import com.micromart.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository - Data Access Layer for Orders.
 * <p>
 * PEAA Pattern: Repository
 * <p>
 * Demonstrates:
 * - @EntityGraph for eager loading to avoid N+1 queries
 * - Custom JPQL queries
 * - Pagination support
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find order with items eagerly loaded.
     * Uses @EntityGraph to avoid N+1 problem.
     */
    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findWithItemsById(Long id);

    /**
     * Find order with items by order number.
     */
    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findWithItemsByOrderNumber(String orderNumber);

    /**
     * Find orders by user ID.
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * Find orders by user ID with items.
     */
    @EntityGraph(attributePaths = {"items"})
    List<Order> findWithItemsByUserId(Long userId);

    /**
     * Find orders by status.
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Find orders by user and status.
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Find orders created within a date range.
     */
    @Query("SELECT o FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count orders by status.
     */
    long countByStatus(OrderStatus status);

    /**
     * Count orders by user.
     */
    long countByUserId(Long userId);

    /**
     * Find recent orders for a user.
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.orderedAt DESC")
    List<Order> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Check if user has any orders.
     */
    boolean existsByUserId(Long userId);

    /**
     * Find pending orders older than specified time (for cleanup/timeout).
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.orderedAt < :cutoffTime")
    List<Order> findStalePendingOrders(@Param("cutoffTime") LocalDateTime cutoffTime);
}
