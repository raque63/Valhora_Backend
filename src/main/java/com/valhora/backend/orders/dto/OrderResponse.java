package com.valhora.backend.orders.dto;

import com.valhora.backend.orders.DeliveryMethod;
import com.valhora.backend.orders.OrderStatus;
import com.valhora.backend.orders.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        Long orderNumber,
        OrderStatus status,
        String customerName,
        String customerPhone,
        String customerEmail,
        String province,
        String canton,
        String district,
        DeliveryMethod deliveryMethod,
        PaymentMethod paymentMethod,
        String paymentProofUrl,
        String shippingAddress,
        String customerNote,
        List<OrderItemResponse> items,
        BigDecimal itemsSubtotal,
        BigDecimal shippingCost,
        BigDecimal total,
        Instant createdAt
) {
}
