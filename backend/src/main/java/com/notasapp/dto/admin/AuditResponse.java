package com.notasapp.dto.admin;

import java.time.Instant;

public record AuditResponse(
        Long id,
        Long usuarioId,
        String username,
        String accion,
        String entidad,
        Long entidadId,
        String ip,
        String userAgent,
        Instant createdAt
) {
}
