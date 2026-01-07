package com.micromart.order.mapper;

import com.micromart.order.domain.Order;
import com.micromart.order.domain.OrderItem;
import com.micromart.order.domain.valueobject.Address;
import com.micromart.order.dto.request.AddressRequest;
import com.micromart.order.dto.response.AddressResponse;
import com.micromart.order.dto.response.OrderItemResponse;
import com.micromart.order.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Order entity.
 * PEAA Pattern: Data Transfer Object mapping.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "shippingAddress", source = "shippingAddress")
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    AddressResponse toAddressResponse(Address address);

    @Mapping(target = "subtotal", expression = "java(item.getSubtotal())")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    Address toAddress(AddressRequest request);
}
