package com.valhora.backend.categories;

import com.valhora.backend.categories.dto.BrandResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    BrandResponse toResponse(Brand brand);
}
