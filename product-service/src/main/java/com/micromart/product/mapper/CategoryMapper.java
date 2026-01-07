package com.micromart.product.mapper;

import com.micromart.product.domain.Category;
import com.micromart.product.dto.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for Category entity.
 * PEAA Pattern: Data Transfer Object mapping.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "productCount", source = "category", qualifiedByName = "productCount")
    CategoryResponse toResponse(Category category);

    @Named("productCount")
    default Integer productCount(Category category) {
        return category.getProducts() != null ? category.getProducts().size() : 0;
    }
}
