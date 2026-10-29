package com.notasapp.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        @Pattern(regexp = "^[\\p{L}0-9 _-]+$", message = "El tag contiene caracteres no permitidos")
        String nombre
) {
}
