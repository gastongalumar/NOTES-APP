package com.notasapp.service.impl;

import com.notasapp.dto.PageResponse;
import com.notasapp.dto.notification.NotificationResponse;
import com.notasapp.exception.NotFoundException;
import com.notasapp.model.Notificacion;
import com.notasapp.repository.NotificacionRepository;
import com.notasapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificacionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(Long usuarioId, boolean unread, Pageable pageable) {
        var page = unread
                ? repository.findByUsuarioIdAndLeidaAtIsNullOrderByCreatedAtDesc(usuarioId, pageable)
                : repository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long usuarioId) {
        return repository.countByUsuarioIdAndLeidaAtIsNull(usuarioId);
    }

    @Override
    @Transactional
    public void markRead(Long id, Long usuarioId) {
        Notificacion notification = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada"));
        if (notification.getLeidaAt() == null) {
            notification.setLeidaAt(Instant.now());
            repository.save(notification);
        }
    }

    private NotificationResponse toResponse(Notificacion notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTipo(),
                notification.getPayload(),
                notification.getLeidaAt(),
                notification.getCreatedAt()
        );
    }
}
