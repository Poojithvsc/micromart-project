package com.micromart.order.dto.response;

import com.micromart.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Order.
 * PEAA Pattern: Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long userId;
    private OrderStatus status;
    private AddressResponse shippingAddress;
    private BigDecimal totalAmount;
    private String currency;
    private List<OrderItemResponse> items;
    private String notes;
    private LocalDateTime orderedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
