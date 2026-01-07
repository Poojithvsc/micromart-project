package com.micromart.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Product data from Product Service.
 * Used by Feign client to deserialize responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;
    private String name;
    private String sku;
    private BigDecimal priceAmount;
    private String priceCurrency;
    private boolean active;
}
