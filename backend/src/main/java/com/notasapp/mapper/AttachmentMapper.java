package com.notasapp.mapper;

import com.notasapp.dto.attachment.AttachmentResponse;
import com.notasapp.model.Adjunto;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentResponse toResponse(Adjunto adjunto, String downloadUrl) {
        return new AttachmentResponse(
                adjunto.getId(),
                adjunto.getNota().getId(),
                adjunto.getNombreOriginal(),
                adjunto.getMimeType(),
                adjunto.getTamanoBytes(),
                adjunto.getChecksumSha256(),
                adjunto.getEstado(),
                adjunto.getScanStatus(),
                adjunto.getCreatedAt(),
                downloadUrl
        );
    }
}
