package com.valhora.backend.config;

import com.valhora.backend.common.exception.ApiError;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

final class SecurityErrorWriter {

    private SecurityErrorWriter() {
    }

    static void write(
            ObjectMapper objectMapper,
            HttpServletResponse response,
            HttpStatus status,
            String message,
            String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, List.of());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
