package com.notasapp.audit;

import com.notasapp.model.AuditLog;
import com.notasapp.repository.AuditLogRepository;
import com.notasapp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;
    private final UsuarioRepository usuarioRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(AuditEvent event) {
        try {
            AuditLog logEntry = new AuditLog();
            if (event.usuarioId() != null) {
                usuarioRepository.findById(event.usuarioId()).ifPresent(logEntry::setUsuario);
            }
            logEntry.setAccion(event.accion());
            logEntry.setEntidad(event.entidad());
            logEntry.setEntidadId(event.entidadId());
            logEntry.setIp(event.ip());
            logEntry.setUserAgent(trim(event.userAgent()));
            logEntry.setCreatedAt(Instant.now());
            auditLogRepository.save(logEntry);
        } catch (RuntimeException ex) {
            // La auditoría es best-effort: nunca debe tumbar la operación de negocio.
            log.warn("No se pudo persistir el evento de auditoría {}", event.accion(), ex);
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 255 ? value : value.substring(0, 255);
    }
}
