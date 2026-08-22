package com.valhora.backend.products.dto;

import com.valhora.backend.products.Availability;
import com.valhora.backend.products.Gender;
import com.valhora.backend.products.Movement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "El SKU es obligatorio")
        String sku,

        String description,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        BigDecimal price,

        @NotNull(message = "La marca es obligatoria")
        UUID brandId,

        UUID categoryId,

        @NotNull(message = "El género es obligatorio")
        Gender gender,

        @NotNull(message = "El movimiento es obligatorio")
        Movement movement,

        @NotBlank(message = "El material es obligatorio")
        String material,

        @NotBlank(message = "El material de la correa es obligatorio")
        String strapMaterial,

        @NotBlank(message = "El color es obligatorio")
        String color,

        String collection,

        String movementDetail,

        String caliber,

        String powerReserve,

        String caseDiameter,

        String thickness,

        String crystal,

        String waterResistance,

        @PositiveOrZero(message = "El stock no puede ser negativo")
        int stock,

        @NotNull(message = "La disponibilidad es obligatoria")
        Availability availability,

        boolean isNew,

        boolean isBestSeller
) {
}
