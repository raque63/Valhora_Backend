package com.valhora.backend.users.dto;

import com.valhora.backend.users.Role;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role
) {
}
