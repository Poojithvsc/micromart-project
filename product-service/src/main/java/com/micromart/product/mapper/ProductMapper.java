package com.micromart.product.mapper;

import com.micromart.product.domain.Product;
import com.micromart.product.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

/**
 * Product Mapper - Converts between entities and DTOs.
 * <p>
 * PEAA Pattern: Data Transfer Object
 * Maps domain objects to DTOs for API responses.
 */
@Component
public class ProductMapper {

    /**
     * Map Product entity to ProductResponse DTO.
     */
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice() != null ? product.getPrice().getAmount() : null)
                .currency(product.getPrice() != null ? product.getPrice().getCurrency() : null)
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
