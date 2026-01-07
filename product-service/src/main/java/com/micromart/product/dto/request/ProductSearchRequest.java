package com.micromart.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Search Request DTO.
 * <p>
 * Used for dynamic product search with JPA Specifications.
 * All fields are optional - null fields are ignored in search.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

    private String name;
    private String sku;
    private String searchTerm; // Searches name and description
    private Long categoryId;
    private String categoryName;
    private List<Long> categoryIds;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean active;
    private Boolean hasImage;
}
