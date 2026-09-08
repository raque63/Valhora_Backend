package com.valhora.backend.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID productId,
        String name,
        String sku,
        BigDecimal price,
        String thumbnailUrl,
        int quantity,
        BigDecimal subtotal
) {
}
