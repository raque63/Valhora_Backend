package com.valhora.backend.categories.dto;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(
        @NotBlank(message = "El nombre de la marca es obligatorio")
        String name,

        String description
) {
}
