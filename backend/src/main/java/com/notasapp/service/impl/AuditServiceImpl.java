package com.notasapp.service.impl;

import com.notasapp.model.AuditLog;
import com.notasapp.repository.AuditLogRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String accion, Long usuarioId, String entidad, Long entidadId, String ip, String userAgent) {
        AuditLog log = new AuditLog();
        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(log::setUsuario);
        }
        log.setAccion(accion);
        log.setEntidad(entidad);
        log.setEntidadId(entidadId);
        log.setIp(ip);
        log.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 255)));
        log.setCreatedAt(Instant.now());
        auditLogRepository.save(log);
    }
}
