package com.notasapp.dto.admin;

import jakarta.validation.constraints.NotNull;

public record AdminUserEnabledRequest(
        @NotNull(message = "El estado es obligatorio")
        Boolean enabled
) {
}
