package com.valhora.backend.reviews.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        String authorName,
        int rating,
        String comment,
        Instant createdAt
) {
}
