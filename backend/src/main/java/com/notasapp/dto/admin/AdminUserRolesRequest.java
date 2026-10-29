package com.notasapp.dto.admin;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AdminUserRolesRequest(
        @NotEmpty(message = "Debe indicar al menos un rol")
        Set<String> roles
) {
}
