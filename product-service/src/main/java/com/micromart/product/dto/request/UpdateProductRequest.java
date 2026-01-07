package com.micromart.product.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Update Product Request DTO.
 * All fields are optional - only non-null fields are updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    private Long categoryId;

    private String imageUrl;

    private Boolean active;
}
