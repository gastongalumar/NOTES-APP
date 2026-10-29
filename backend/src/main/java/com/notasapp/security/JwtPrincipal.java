package com.notasapp.security;

import java.util.Set;

public record JwtPrincipal(Long userId, String username, Set<String> roles) {
    public boolean isAdmin() {
        return roles.contains("ADMIN");
    }
}
