package com.notasapp.service.impl;

import com.notasapp.config.MailProperties;
import com.notasapp.dto.note.NoteResponse;
import com.notasapp.dto.share.ShareRequest;
import com.notasapp.dto.share.ShareResponse;
import com.notasapp.exception.GoneException;
import com.notasapp.exception.NotFoundException;
import com.notasapp.exception.UnprocessableException;
import com.notasapp.mapper.NoteMapper;
import com.notasapp.model.Nota;
import com.notasapp.model.SharedLink;
import com.notasapp.repository.NotaRepository;
import com.notasapp.repository.SharedLinkRepository;
import com.notasapp.service.AuditService;
import com.notasapp.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final NotaRepository notaRepository;
    private final SharedLinkRepository sharedLinkRepository;
    private final NoteMapper noteMapper;
    private final MailProperties mailProperties;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public ShareResponse create(Long noteId, Long usuarioId, ShareRequest request) {
        Nota nota = notaRepository.findByIdAndUsuarioIdAndDeletedAtIsNull(noteId, usuarioId)
                .orElseThrow(() -> new NotFoundException("Nota no encontrada"));
        Instant expiresAt = request == null || request.expiresAt() == null
                ? Instant.now().plus(7, ChronoUnit.DAYS)
                : request.expiresAt();
        if (!expiresAt.isAfter(Instant.now())) {
            throw new UnprocessableException("INVALID_EXPIRY", "La expiración debe ser futura");
        }
        String raw = randomToken();
        SharedLink link = new SharedLink();
        link.setNota(nota);
        link.setTokenHash(hash(raw));
        link.setExpiresAt(expiresAt);
        link.setCreatedAt(Instant.now());
        sharedLinkRepository.save(link);
        auditService.log("SHARE_CREATE", usuarioId, "NOTA", noteId, null, null);
        return new ShareResponse(mailProperties.getFrontendUrl() + "/share/" + raw, expiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse publicNote(String rawToken) {
        SharedLink link = sharedLinkRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new NotFoundException("Enlace no encontrado"));
        if (link.getRevokedAt() != null || link.getExpiresAt().isBefore(Instant.now())) {
            throw new GoneException("El enlace expiró o fue revocado");
        }
        Nota nota = link.getNota();
        if (nota.getDeletedAt() != null) {
            throw new GoneException("La nota ya no está disponible");
        }
        return noteMapper.toResponse(nota);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
