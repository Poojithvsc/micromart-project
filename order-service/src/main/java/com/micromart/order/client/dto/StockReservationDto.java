package com.micromart.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock reservation requests to inventory service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationDto {

    private Long productId;
    private Integer quantity;
    private String orderReference;
}
