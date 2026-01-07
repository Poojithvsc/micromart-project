package com.micromart.product.service;

import com.micromart.common.exception.DuplicateResourceException;
import com.micromart.common.exception.ResourceNotFoundException;
import com.micromart.product.domain.Category;
import com.micromart.product.domain.Product;
import com.micromart.product.domain.valueobject.Money;
import com.micromart.product.dto.request.CreateProductRequest;
import com.micromart.product.dto.request.ProductSearchRequest;
import com.micromart.product.dto.request.UpdateProductRequest;
import com.micromart.product.repository.CategoryRepository;
import com.micromart.product.repository.ProductRepository;
import com.micromart.product.repository.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product Service Implementation.
 * <p>
 * Demonstrates:
 * - JPA Specifications for dynamic queries
 * - Service Layer pattern
 * - Transaction management
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        // Check for duplicate SKU
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        // Find category if specified
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        // Create product with Money value object
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(Money.of(request.getPrice(), request.getCurrency()))
                .imageUrl(request.getImageUrl())
                .category(category)
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Created product with ID: {}", savedProduct.getId());

        return savedProduct;
    }

    @Override
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * Search products using JPA Specifications.
     * <p>
     * PEAA Pattern: Specification
     * Combines multiple predicates into a composite specification.
     * Allows building dynamic queries based on search criteria.
     */
    @Override
    public Page<Product> search(ProductSearchRequest request, Pageable pageable) {
        log.debug("Searching products with criteria: {}", request);

        // Build composite specification from search criteria
        Specification<Product> spec = Specification.where(null);

        // Add name filter
        if (request.getName() != null) {
            spec = spec.and(ProductSpecification.hasName(request.getName()));
        }

        // Add SKU filter
        if (request.getSku() != null) {
            spec = spec.and(ProductSpecification.hasSku(request.getSku()));
        }

        // Add text search (name and description)
        if (request.getSearchTerm() != null) {
            spec = spec.and(ProductSpecification.searchByNameOrDescription(request.getSearchTerm()));
        }

        // Add category filter
        if (request.getCategoryId() != null) {
            spec = spec.and(ProductSpecification.inCategory(request.getCategoryId()));
        }

        // Add category name filter
        if (request.getCategoryName() != null) {
            spec = spec.and(ProductSpecification.inCategoryNamed(request.getCategoryName()));
        }

        // Add multiple categories filter
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            spec = spec.and(ProductSpecification.inCategories(request.getCategoryIds()));
        }

        // Add price range filter
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            spec = spec.and(ProductSpecification.priceRange(request.getMinPrice(), request.getMaxPrice()));
        }

        // Add active filter
        if (request.getActive() != null) {
            if (request.getActive()) {
                spec = spec.and(ProductSpecification.isActive());
            } else {
                spec = spec.and(ProductSpecification.isInactive());
            }
        }

        // Add image filter
        if (request.getHasImage() != null) {
            if (request.getHasImage()) {
                spec = spec.and(ProductSpecification.hasImage());
            } else {
                spec = spec.and(ProductSpecification.noImage());
            }
        }

        return productRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = getById(id);

        // Update fields if provided
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null && request.getCurrency() != null) {
            product.updatePrice(Money.of(request.getPrice(), request.getCurrency()));
        } else if (request.getPrice() != null) {
            // Keep existing currency
            product.updatePrice(Money.of(request.getPrice(), product.getPrice().getCurrency()));
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getActive() != null) {
            if (request.getActive()) {
                product.activate();
            } else {
                product.deactivate();
            }
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        Product product = getById(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public Product activateProduct(Long id) {
        log.info("Activating product with ID: {}", id);
        Product product = getById(id);
        product.activate();
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product deactivateProduct(Long id) {
        log.info("Deactivating product with ID: {}", id);
        Product product = getById(id);
        product.deactivate();
        return productRepository.save(product);
    }

    @Override
    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public BigDecimal getProductPrice(Long productId) {
        Product product = getById(productId);
        return product.getPrice() != null ? product.getPrice().getAmount() : BigDecimal.ZERO;
    }

    @Override
    public boolean isSkuAvailable(String sku) {
        return !productRepository.existsBySku(sku);
    }
}
