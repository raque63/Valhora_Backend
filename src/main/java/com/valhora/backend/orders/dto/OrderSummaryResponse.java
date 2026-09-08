package com.valhora.backend.orders.dto;

import com.valhora.backend.orders.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        Long orderNumber,
        OrderStatus status,
        String customerName,
        int itemCount,
        BigDecimal total,
        Instant createdAt
) {
}
