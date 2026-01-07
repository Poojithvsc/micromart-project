package com.micromart.product.repository;

import com.micromart.product.domain.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository - Data Access Layer for Products.
 * <p>
 * PEAA Pattern: Repository
 * Mediates between domain and data mapping layers.
 * <p>
 * Spring Data JPA:
 * - JpaRepository: Standard CRUD + pagination
 * - JpaSpecificationExecutor: Dynamic queries with Specifications
 * - @EntityGraph: Optimize fetch strategies
 * - @Query: Custom JPQL queries
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Find product by SKU.
     *
     * @param sku Product SKU
     * @return Optional containing product if found
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find product by SKU with category eagerly loaded.
     *
     * @param sku Product SKU
     * @return Optional containing product if found
     */
    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findWithCategoryBySku(String sku);

    /**
     * Find all active products.
     *
     * @return List of active products
     */
    List<Product> findByActiveTrue();

    /**
     * Find products by category ID.
     *
     * @param categoryId Category ID
     * @return List of products in category
     */
    @EntityGraph(attributePaths = {"category"})
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Find products by name containing (case-insensitive).
     *
     * @param name Name pattern
     * @return List of matching products
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Check if SKU exists.
     *
     * @param sku SKU to check
     * @return true if SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Count products by category.
     *
     * @param categoryId Category ID
     * @return Number of products in category
     */
    long countByCategoryId(Long categoryId);

    /**
     * Find top N expensive products.
     *
     * @param limit Number of products to return
     * @return List of expensive products
     */
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.price.amount DESC LIMIT :limit")
    List<Product> findTopExpensiveProducts(@Param("limit") int limit);

    /**
     * Find products without category.
     *
     * @return List of uncategorized products
     */
    @Query("SELECT p FROM Product p WHERE p.category IS NULL")
    List<Product> findUncategorizedProducts();
}
