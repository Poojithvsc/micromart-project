package com.micromart.product.repository;

import com.micromart.product.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Inventory Repository - Data access for inventory management.
 * <p>
 * PEAA Pattern: Repository
 * <p>
 * Learning Points:
 * - @Lock for pessimistic locking in high-contention scenarios
 * - Custom JPQL queries with @Query
 * - Different lock modes for different use cases
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory by product ID.
     */
    Optional<Inventory> findByProductId(Long productId);

    /**
     * Find inventory by product ID with pessimistic write lock.
     * Use this when updating inventory to prevent race conditions.
     * <p>
     * PESSIMISTIC_WRITE: Exclusive lock, blocks other reads and writes.
     * Use for critical operations like stock reservation.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Optional<Inventory> findByProductIdForUpdate(@Param("productId") Long productId);

    /**
     * Find inventory by product SKU.
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.sku = :sku")
    Optional<Inventory> findByProductSku(@Param("sku") String sku);

    /**
     * Find all inventory items needing reorder.
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderLevel")
    List<Inventory> findNeedingReorder();

    /**
     * Find out of stock products.
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity - i.reservedQuantity <= 0")
    List<Inventory> findOutOfStock();

    /**
     * Find inventory by category.
     */
    @Query("SELECT i FROM Inventory i WHERE i.product.category.id = :categoryId")
    List<Inventory> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Check if product has sufficient available stock.
     */
    @Query("SELECT CASE WHEN (i.quantity - i.reservedQuantity) >= :quantity THEN true ELSE false END " +
           "FROM Inventory i WHERE i.product.id = :productId")
    boolean hasAvailableStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Get total reserved quantity for a product.
     */
    @Query("SELECT COALESCE(i.reservedQuantity, 0) FROM Inventory i WHERE i.product.id = :productId")
    Integer getReservedQuantity(@Param("productId") Long productId);

    /**
     * Bulk check for multiple products.
     * Returns product IDs that don't have sufficient stock.
     */
    @Query("SELECT i.product.id FROM Inventory i WHERE i.product.id IN :productIds " +
           "AND (i.quantity - i.reservedQuantity) < :requiredQuantity")
    List<Long> findInsufficientStockProducts(
            @Param("productIds") List<Long> productIds,
            @Param("requiredQuantity") Integer requiredQuantity);
}
