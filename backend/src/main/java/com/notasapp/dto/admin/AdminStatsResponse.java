package com.notasapp.dto.admin;

public record AdminStatsResponse(
        long usuarios,
        long notas,
        long notasArchivadas,
        long adjuntos,
        long loginsExitosos,
        long loginsFallidos
) {
}
