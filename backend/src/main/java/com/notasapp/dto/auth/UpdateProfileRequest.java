package com.notasapp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 80, message = "El nombre es demasiado largo")
        String nombre,

        @Email(message = "El email no es válido")
        @Size(max = 254, message = "El email es demasiado largo")
        String email
) {
}
