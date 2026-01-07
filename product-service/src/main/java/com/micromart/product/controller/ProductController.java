package com.micromart.product.controller;

import com.micromart.common.dto.ApiResponse;
import com.micromart.common.dto.PageResponse;
import com.micromart.product.dto.request.CreateProductRequest;
import com.micromart.product.dto.request.ProductSearchRequest;
import com.micromart.product.dto.request.UpdateProductRequest;
import com.micromart.product.dto.response.ProductResponse;
import com.micromart.product.mapper.ProductMapper;
import com.micromart.product.service.ProductService;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Controller - REST API for Products.
 * <p>
 * PEAA Pattern: Remote Facade
 * Provides coarse-grained REST interface over product service.
 * <p>
 * Demonstrates:
 * - Spring MVC annotations
 * - Pagination with @PageableDefault
 * - Search using JPA Specifications
 * - @PreAuthorize for method-level security
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    /**
     * Create a new product.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product", description = "Create a new product (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        log.info("Creating product: {}", request.getSku());
        var product = productService.createProduct(request);
        var response = productMapper.toResponse(product);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully"));
    }

    /**
     * Get product by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product", description = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        var product = productService.getById(id);
        var response = productMapper.toResponse(product);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get product by SKU.
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Get product by SKU code")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(@PathVariable String sku) {
        var product = productService.findBySku(sku)
                .orElseThrow(() -> new com.micromart.common.exception.ResourceNotFoundException("Product", "sku", sku));
        var response = productMapper.toResponse(product);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all products with pagination.
     */
    @GetMapping
    @Operation(summary = "List products", description = "Get all products with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var page = productService.findAll(pageable);
        var responses = page.map(productMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    /**
     * Search products with dynamic filters.
     * Uses JPA Specifications for flexible querying.
     */
    @PostMapping("/search")
    @Operation(summary = "Search products", description = "Search products with dynamic filters using Specifications")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestBody ProductSearchRequest searchRequest,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("Searching products with criteria: {}", searchRequest);
        var page = productService.search(searchRequest, pageable);
        var responses = page.map(productMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    /**
     * Update product.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product", description = "Update product details (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        log.info("Updating product: {}", id);
        var product = productService.updateProduct(id, request);
        var response = productMapper.toResponse(product);
        return ResponseEntity.ok(ApiResponse.success(response, "Product updated successfully"));
    }

    /**
     * Delete product.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product", description = "Delete a product (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    /**
     * Activate product.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate product", description = "Activate a product (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> activateProduct(@PathVariable Long id) {
        var product = productService.activateProduct(id);
        var response = productMapper.toResponse(product);
        return ResponseEntity.ok(ApiResponse.success(response, "Product activated"));
    }

    /**
     * Deactivate product.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate product", description = "Deactivate a product (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivateProduct(@PathVariable Long id) {
        var product = productService.deactivateProduct(id);
        var response = productMapper.toResponse(product);
        return ResponseEntity.ok(ApiResponse.success(response, "Product deactivated"));
    }

    /**
     * Get products by category.
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Get all products in a category")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId
    ) {
        var products = productService.getByCategory(categoryId);
        var responses = products.stream()
                .map(productMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get product price.
     * Used by order-service via Feign client.
     */
    @GetMapping("/{id}/price")
    @Operation(summary = "Get product price", description = "Get product price (for inter-service calls)")
    public ResponseEntity<BigDecimal> getProductPrice(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductPrice(id));
    }

    /**
     * Check SKU availability.
     */
    @GetMapping("/check-sku")
    @Operation(summary = "Check SKU availability", description = "Check if SKU is available")
    public ResponseEntity<ApiResponse<Boolean>> checkSkuAvailability(@RequestParam String sku) {
        boolean available = productService.isSkuAvailable(sku);
        return ResponseEntity.ok(ApiResponse.success(available,
                available ? "SKU is available" : "SKU is already taken"));
    }
}
