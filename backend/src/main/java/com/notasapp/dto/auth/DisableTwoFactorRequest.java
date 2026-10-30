package com.notasapp.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record DisableTwoFactorRequest(
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
