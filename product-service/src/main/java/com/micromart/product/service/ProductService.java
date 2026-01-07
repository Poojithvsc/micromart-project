package com.micromart.product.service;

import com.micromart.product.domain.Product;
import com.micromart.product.dto.request.CreateProductRequest;
import com.micromart.product.dto.request.ProductSearchRequest;
import com.micromart.product.dto.request.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product Service Interface - Service Layer for Products.
 * <p>
 * PEAA Patterns:
 * - Service Layer: Coordinates application logic
 * - Separated Interface: Interface separate from implementation
 */
public interface ProductService {

    /**
     * Create a new product.
     */
    Product createProduct(CreateProductRequest request);

    /**
     * Get product by ID.
     */
    Product getById(Long id);

    /**
     * Find product by ID.
     */
    Optional<Product> findById(Long id);

    /**
     * Find product by SKU.
     */
    Optional<Product> findBySku(String sku);

    /**
     * Get all products with pagination.
     */
    Page<Product> findAll(Pageable pageable);

    /**
     * Search products with dynamic filters using Specifications.
     */
    Page<Product> search(ProductSearchRequest searchRequest, Pageable pageable);

    /**
     * Update product.
     */
    Product updateProduct(Long id, UpdateProductRequest request);

    /**
     * Delete product.
     */
    void deleteProduct(Long id);

    /**
     * Activate product.
     */
    Product activateProduct(Long id);

    /**
     * Deactivate product.
     */
    Product deactivateProduct(Long id);

    /**
     * Get products by category.
     */
    List<Product> getByCategory(Long categoryId);

    /**
     * Get product price.
     */
    BigDecimal getProductPrice(Long productId);

    /**
     * Check if SKU is available.
     */
    boolean isSkuAvailable(String sku);
}
