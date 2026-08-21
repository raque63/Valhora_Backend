package com.valhora.backend.products;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductFilter(
        UUID brandId,
        UUID categoryId,
        Gender gender,
        Movement movement,
        String material,
        String strapMaterial,
        String color,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Availability availability
) {
}
