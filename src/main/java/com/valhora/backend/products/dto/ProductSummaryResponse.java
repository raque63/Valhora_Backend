package com.valhora.backend.products.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal price,
        String brandName,
        String thumbnailUrl,
        boolean isNew,
        boolean isBestSeller
) {
}
