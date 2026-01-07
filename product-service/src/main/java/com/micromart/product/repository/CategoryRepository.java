package com.micromart.product.repository;

import com.micromart.product.domain.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Category Repository - Data Access Layer for Categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name.
     */
    Optional<Category> findByName(String name);

    /**
     * Find category by name (case-insensitive).
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Find category with products eagerly loaded.
     */
    @EntityGraph(attributePaths = {"products"})
    Optional<Category> findWithProductsById(Long id);

    /**
     * Check if category name exists.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find categories by name containing.
     */
    List<Category> findByNameContainingIgnoreCase(String name);
}
