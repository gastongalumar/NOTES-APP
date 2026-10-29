package com.notasapp.dto.auth;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String nombre,
        boolean enabled,
        boolean emailVerified,
        Set<String> roles,
        Instant createdAt
) {
}
