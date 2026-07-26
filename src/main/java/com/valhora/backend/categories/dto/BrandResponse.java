package com.valhora.backend.categories.dto;

import java.util.UUID;

public record BrandResponse(
        UUID id,
        String name,
        String slug
) {
}
