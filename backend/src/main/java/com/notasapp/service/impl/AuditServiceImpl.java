package com.notasapp.service.impl;

import com.notasapp.audit.AuditEvent;
import com.notasapp.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void log(String accion, Long usuarioId, String entidad, Long entidadId, String ip, String userAgent) {
        // El listener persiste después del commit para no competir por el lock
        // del usuario que mantiene el upload con SELECT ... FOR UPDATE.
        eventPublisher.publishEvent(new AuditEvent(accion, usuarioId, entidad, entidadId, ip, userAgent));
    }
}
