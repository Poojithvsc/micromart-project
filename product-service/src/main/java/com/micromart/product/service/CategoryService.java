package com.micromart.product.service;

import com.micromart.product.domain.Category;
import com.micromart.product.dto.request.CreateCategoryRequest;
import com.micromart.product.dto.request.UpdateCategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Category Service Interface.
 * <p>
 * PEAA Pattern: Separated Interface
 * Allows different implementations and easier testing via mocking.
 */
public interface CategoryService {

    Category createCategory(CreateCategoryRequest request);

    Category getById(Long id);

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    Page<Category> findAll(Pageable pageable);

    List<Category> findAllList();

    Category updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);

    boolean existsByName(String name);
}
