package com.notasapp.dto.category;

public record CategoryResponse(
        Long id,
        String nombre,
        String color,
        String descripcion,
        long cantidadNotas
) {
}
