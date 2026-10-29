package com.notasapp.audit;

public record AuditEvent(
        String accion,
        Long usuarioId,
        String entidad,
        Long entidadId,
        String ip,
        String userAgent
) {
}
