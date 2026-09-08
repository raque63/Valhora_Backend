package com.valhora.backend.categories.dto;

import java.util.List;
import java.util.UUID;

public record BrandResponse(
        UUID id,
        String name,
        String slug,
        String logoUrl,
        String description,
        List<String> imageUrls
) {
}
