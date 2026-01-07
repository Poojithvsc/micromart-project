package com.micromart.product.service;

import com.micromart.common.exception.DuplicateResourceException;
import com.micromart.common.exception.ResourceNotFoundException;
import com.micromart.product.domain.Category;
import com.micromart.product.dto.request.CreateCategoryRequest;
import com.micromart.product.dto.request.UpdateCategoryRequest;
import com.micromart.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Category Service Implementation.
 * <p>
 * PEAA Patterns:
 * - Service Layer: Business logic encapsulation
 * - Repository: Data access abstraction
 * - Unit of Work: @Transactional for transaction management
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(CreateCategoryRequest request) {
        log.info("Creating category: {}", request.getName());

        // Check for duplicate name
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category with ID: {}", saved.getId());
        return saved;
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name);
    }

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public List<Category> findAllList() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category with ID: {}", id);

        Category category = getById(id);

        if (request.getName() != null) {
            // Check for duplicate name (excluding current category)
            categoryRepository.findByNameIgnoreCase(request.getName())
                    .filter(c -> !c.getId().equals(id))
                    .ifPresent(c -> {
                        throw new DuplicateResourceException("Category", "name", request.getName());
                    });
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);
        Category category = getById(id);

        // Check if category has products
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException(
                    String.format("Cannot delete category '%s' with %d products. Remove products first.",
                            category.getName(), category.getProducts().size()));
        }

        categoryRepository.delete(category);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }
}
