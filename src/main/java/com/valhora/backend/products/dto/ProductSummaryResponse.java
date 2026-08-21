package com.valhora.backend.products.dto;

import com.valhora.backend.products.Availability;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal price,
        String brandName,
        String thumbnailUrl,
        Availability availability,
        boolean isNew,
        boolean isBestSeller
) {
}
