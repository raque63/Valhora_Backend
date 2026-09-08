package com.valhora.backend.orders.dto;

import com.valhora.backend.orders.DeliveryMethod;
import com.valhora.backend.orders.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String customerName,

        @NotBlank(message = "El teléfono es obligatorio")
        String customerPhone,

        @NotBlank(message = "El correo es obligatorio")
        String customerEmail,

        @NotBlank(message = "La provincia es obligatoria")
        String province,

        @NotBlank(message = "El cantón es obligatorio")
        String canton,

        @NotBlank(message = "El distrito es obligatorio")
        String district,

        String shippingAddress,

        String customerNote,

        @NotNull(message = "El método de entrega es obligatorio")
        DeliveryMethod deliveryMethod,

        @NotNull(message = "El método de pago es obligatorio")
        PaymentMethod paymentMethod
) {
}
