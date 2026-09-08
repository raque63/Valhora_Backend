package com.valhora.backend.orders.dto;

import com.valhora.backend.orders.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        String customerName,
        String customerEmail,
        String shippingAddress,
        String customerNote,
        List<OrderItemResponse> items,
        BigDecimal itemsSubtotal,
        BigDecimal shippingCost,
        BigDecimal total,
        Instant createdAt
) {
}
