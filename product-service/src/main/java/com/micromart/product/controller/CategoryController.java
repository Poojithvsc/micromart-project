package com.micromart.product.controller;

import com.micromart.common.dto.ApiResponse;
import com.micromart.common.dto.PageResponse;
import com.micromart.product.dto.request.CreateCategoryRequest;
import com.micromart.product.dto.request.UpdateCategoryRequest;
import com.micromart.product.dto.response.CategoryResponse;
import com.micromart.product.mapper.CategoryMapper;
import com.micromart.product.service.CategoryService;
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
 * Category Controller - REST API for Categories.
 * <p>
 * PEAA Pattern: Remote Facade
 * Provides coarse-grained REST interface over category service.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    /**
     * Create a new category.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create category", description = "Create a new category (Admin only)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        log.info("Creating category: {}", request.getName());
        var category = categoryService.createCategory(request);
        var response = categoryMapper.toResponse(category);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Category created successfully"));
    }

    /**
     * Get category by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category", description = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable Long id) {
        var category = categoryService.getById(id);
        var response = categoryMapper.toResponse(category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all categories with pagination.
     */
    @GetMapping
    @Operation(summary = "List categories", description = "Get all categories with pagination")
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getAllCategories(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var page = categoryService.findAll(pageable);
        var responses = page.map(categoryMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responses)));
    }

    /**
     * Get all categories as a simple list (no pagination).
     * Useful for dropdown menus.
     */
    @GetMapping("/all")
    @Operation(summary = "List all categories", description = "Get all categories as a list (for dropdowns)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategoriesList() {
        var categories = categoryService.findAllList();
        var responses = categories.stream()
                .map(categoryMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Update category.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category", description = "Update category details (Admin only)")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        log.info("Updating category: {}", id);
        var category = categoryService.updateCategory(id, request);
        var response = categoryMapper.toResponse(category);
        return ResponseEntity.ok(ApiResponse.success(response, "Category updated successfully"));
    }

    /**
     * Delete category.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category", description = "Delete a category (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        log.info("Deleting category: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }

    /**
     * Check if category name is available.
     */
    @GetMapping("/check-name")
    @Operation(summary = "Check category name", description = "Check if category name is available")
    public ResponseEntity<ApiResponse<Boolean>> checkNameAvailability(@RequestParam String name) {
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(ApiResponse.success(!exists,
                exists ? "Category name already exists" : "Category name is available"));
    }
}
