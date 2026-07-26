package com.valhora.backend.auth.dto;

import com.valhora.backend.users.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        UserResponse user
) {
}
