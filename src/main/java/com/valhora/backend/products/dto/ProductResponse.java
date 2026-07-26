package com.valhora.backend.products.dto;

import com.valhora.backend.categories.dto.BrandResponse;
import com.valhora.backend.categories.dto.CategoryResponse;
import com.valhora.backend.products.Gender;
import com.valhora.backend.products.Movement;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        String sku,
        String description,
        BigDecimal price,
        BrandResponse brand,
        CategoryResponse category,
        Gender gender,
        Movement movement,
        String material,
        String strapMaterial,
        String color,
        int stock,
        boolean isNew,
        boolean isBestSeller,
        List<String> imageUrls,
        Instant createdAt
) {
}
