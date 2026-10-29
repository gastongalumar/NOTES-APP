package com.notasapp.service;

import com.notasapp.dto.PageResponse;
import com.notasapp.dto.notification.NotificationResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    PageResponse<NotificationResponse> list(Long usuarioId, boolean unread, Pageable pageable);

    long unreadCount(Long usuarioId);

    void markRead(Long id, Long usuarioId);
}
