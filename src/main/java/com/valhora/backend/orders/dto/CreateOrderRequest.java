package com.valhora.backend.orders.dto;

public record CreateOrderRequest(
        String shippingAddress,
        String customerNote
) {
}
