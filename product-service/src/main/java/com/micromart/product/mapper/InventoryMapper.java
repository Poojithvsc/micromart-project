package com.micromart.product.mapper;

import com.micromart.product.domain.Inventory;
import com.micromart.product.dto.response.InventoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Inventory entity.
 * PEAA Pattern: Data Transfer Object mapping.
 */
@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "availableQuantity", expression = "java(inventory.getAvailableQuantity())")
    @Mapping(target = "inStock", expression = "java(inventory.isInStock())")
    @Mapping(target = "needsReorder", expression = "java(inventory.needsReorder())")
    InventoryResponse toResponse(Inventory inventory);
}
