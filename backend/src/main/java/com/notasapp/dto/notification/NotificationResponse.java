package com.notasapp.dto.notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String tipo,
        String payload,
        Instant leidaAt,
        Instant createdAt
) {
}
