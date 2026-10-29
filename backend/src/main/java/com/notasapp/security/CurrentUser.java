package com.notasapp.security;

import com.notasapp.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public JwtPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            throw new UnauthorizedException("Necesitás iniciar sesión");
        }
        return principal;
    }

    public Long id() {
        return principal().userId();
    }

    public String username() {
        return principal().username();
    }

    public boolean isAdmin() {
        return principal().isAdmin();
    }
}
