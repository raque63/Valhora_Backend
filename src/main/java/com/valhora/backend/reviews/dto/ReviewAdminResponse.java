package com.valhora.backend.reviews.dto;

import com.valhora.backend.reviews.ReviewStatus;
import java.time.Instant;
import java.util.UUID;

public record ReviewAdminResponse(
        UUID id,
        String authorName,
        int rating,
        String comment,
        ReviewStatus status,
        Instant createdAt
) {
}
