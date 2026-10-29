package com.notasapp.dto.attachment;

import java.time.Instant;

public record AttachmentResponse(
        Long id,
        Long notaId,
        String nombreOriginal,
        String mimeType,
        long tamanoBytes,
        String checksumSha256,
        String estado,
        String scanStatus,
        Instant createdAt,
        String downloadUrl
) {
}
