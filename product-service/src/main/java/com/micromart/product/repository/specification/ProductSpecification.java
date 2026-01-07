package com.micromart.product.repository.specification;

import com.micromart.product.domain.Category;
import com.micromart.product.domain.Product;
import com.micromart.product.domain.valueobject.Money;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Product Specifications - Dynamic query builder for products.
 * <p>
 * PEAA Pattern: Specification
 * A predicate that determines if an object satisfies some criteria.
 * Combined specifications allow building complex, dynamic queries.
 * <p>
 * Spring Data JPA: JpaSpecificationExecutor
 * Used with repositories that extend JpaSpecificationExecutor<Product>
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * Specification<Product> spec = ProductSpecification.hasName("phone")
 *     .and(ProductSpecification.inCategory(categoryId))
 *     .and(ProductSpecification.priceRange(100, 500))
 *     .and(ProductSpecification.isActive());
 *
 * productRepository.findAll(spec, pageable);
 * }
 * </pre>
 */
public final class ProductSpecification {

    private ProductSpecification() {
        // Utility class - no instantiation
    }

    /**
     * Filter products by name (case-insensitive, contains).
     *
     * @param name Product name to search
     * @return Specification for name filter
     */
    public static Specification<Product> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction(); // Always true
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filter products by SKU (exact match).
     *
     * @param sku Product SKU
     * @return Specification for SKU filter
     */
    public static Specification<Product> hasSku(String sku) {
        return (root, query, criteriaBuilder) -> {
            if (sku == null || sku.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("sku"), sku);
        };
    }

    /**
     * Filter products by category ID.
     *
     * @param categoryId Category ID
     * @return Specification for category filter
     */
    public static Specification<Product> inCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    /**
     * Filter products by category name (partial match).
     *
     * @param categoryName Category name
     * @return Specification for category name filter
     */
    public static Specification<Product> inCategoryNamed(String categoryName) {
        return (root, query, criteriaBuilder) -> {
            if (categoryName == null || categoryName.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
            return criteriaBuilder.like(
                    criteriaBuilder.lower(categoryJoin.get("name")),
                    "%" + categoryName.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filter products within price range.
     *
     * @param minPrice Minimum price (inclusive)
     * @param maxPrice Maximum price (inclusive)
     * @return Specification for price range filter
     */
    public static Specification<Product> priceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            }

            // Access the embedded Money value object's amount with proper typing
            jakarta.persistence.criteria.Path<BigDecimal> priceAmount = root.get("price").get("amount");

            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(priceAmount, minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(priceAmount, minPrice);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(priceAmount, maxPrice);
            }
        };
    }

    /**
     * Filter only active products.
     *
     * @return Specification for active products
     */
    public static Specification<Product> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("active"));
    }

    /**
     * Filter only inactive products.
     *
     * @return Specification for inactive products
     */
    public static Specification<Product> isInactive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("active"));
    }

    /**
     * Filter products with images.
     *
     * @return Specification for products with images
     */
    public static Specification<Product> hasImage() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get("imageUrl"));
    }

    /**
     * Filter products without images.
     *
     * @return Specification for products without images
     */
    public static Specification<Product> noImage() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("imageUrl"));
    }

    /**
     * Search products by name or description (full-text-like search).
     *
     * @param searchTerm Search term
     * @return Specification for text search
     */
    public static Specification<Product> searchByNameOrDescription(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Filter products by multiple category IDs.
     *
     * @param categoryIds List of category IDs
     * @return Specification for multiple categories
     */
    public static Specification<Product> inCategories(java.util.List<Long> categoryIds) {
        return (root, query, criteriaBuilder) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
            return categoryJoin.get("id").in(categoryIds);
        };
    }
}
