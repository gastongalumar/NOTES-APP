package com.notasapp.service;

public interface AuditService {
    void log(String accion, Long usuarioId, String entidad, Long entidadId, String ip, String userAgent);
}
