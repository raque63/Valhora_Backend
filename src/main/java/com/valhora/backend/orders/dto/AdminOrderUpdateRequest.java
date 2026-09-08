package com.valhora.backend.orders.dto;

import com.valhora.backend.orders.OrderStatus;
import java.math.BigDecimal;

public record AdminOrderUpdateRequest(
        OrderStatus status,
        BigDecimal shippingCost
) {
}
