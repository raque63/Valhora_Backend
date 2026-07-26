package com.valhora.backend.auth;

import com.valhora.backend.auth.dto.AuthResponse;

record AuthResult(AuthResponse response, String refreshToken) {
}
