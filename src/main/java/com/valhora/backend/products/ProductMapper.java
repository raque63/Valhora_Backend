package com.valhora.backend.products;

import com.valhora.backend.categories.BrandMapper;
import com.valhora.backend.categories.CategoryMapper;
import com.valhora.backend.products.dto.ProductResponse;
import com.valhora.backend.products.dto.ProductSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BrandMapper.class, CategoryMapper.class})
public interface ProductMapper {

    @Mapping(target = "isNew", source = "new")
    @Mapping(target = "isBestSeller", source = "bestSeller")
    ProductResponse toResponse(Product product);

    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "thumbnailUrl", expression = "java(product.getImageUrls().isEmpty() ? null : product.getImageUrls().get(0))")
    @Mapping(target = "isNew", source = "new")
    @Mapping(target = "isBestSeller", source = "bestSeller")
    ProductSummaryResponse toSummary(Product product);
}
