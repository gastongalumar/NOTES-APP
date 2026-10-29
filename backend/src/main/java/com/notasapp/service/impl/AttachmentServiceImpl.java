package com.notasapp.service.impl;

import com.notasapp.config.AttachmentProperties;
import com.notasapp.config.AntivirusProperties;
import com.notasapp.dto.attachment.AttachmentResponse;
import com.notasapp.exception.ConflictException;
import com.notasapp.exception.GoneException;
import com.notasapp.exception.NotFoundException;
import com.notasapp.exception.PayloadTooLargeException;
import com.notasapp.exception.UnprocessableException;
import com.notasapp.exception.UnsupportedMediaTypeException;
import com.notasapp.mapper.AttachmentMapper;
import com.notasapp.model.Adjunto;
import com.notasapp.model.Nota;
import com.notasapp.model.Usuario;
import com.notasapp.repository.AdjuntoRepository;
import com.notasapp.repository.NotaRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.service.AttachmentService;
import com.notasapp.service.AuditService;
import com.notasapp.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final Set<String> ALLOWED = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif",
            "text/plain",
            "text/markdown",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final AdjuntoRepository adjuntoRepository;
    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;
    private final AttachmentProperties properties;
    private final AntivirusProperties antivirusProperties;
    private final AttachmentMapper mapper;
    private final AuditService auditService;
    private final Tika tika = new Tika();

    @Override
    @Transactional
    public AttachmentResponse upload(Long noteId, Long usuarioId, MultipartFile file, String ip, String userAgent) {
        if (file == null || file.isEmpty()) {
            throw new UnprocessableException("EMPTY_FILE", "El archivo está vacío");
        }
        if (file.getSize() > properties.getMaxFileBytes()) {
            throw new PayloadTooLargeException("El archivo supera el límite de 10 MB");
        }
        Nota nota = notaRepository.findByIdAndUsuarioIdAndDeletedAtIsNull(noteId, usuarioId)
                .orElseThrow(() -> new NotFoundException("Nota no encontrada"));
        usuarioRepository.findForUpdate(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (adjuntoRepository.totalBytesByUsuario(usuarioId) + file.getSize() > properties.getMaxTotalBytes()) {
            throw new ConflictException("QUOTA_EXCEEDED", "Superaste la cuota total de 200 MB");
        }
        if (adjuntoRepository.totalBytesByNota(noteId) + file.getSize() > properties.getMaxPerNoteBytes()) {
            throw new ConflictException("NOTE_QUOTA_EXCEEDED", "La nota superó su cuota de adjuntos");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new UnprocessableException("FILE_READ_ERROR", "No se pudo leer el archivo");
        }
        String original = sanitizeName(file.getOriginalFilename());
        String detected = detect(bytes, original);
        if (!ALLOWED.contains(detected)) {
            throw new UnsupportedMediaTypeException("Tipo de archivo no permitido: " + detected);
        }

        String key = "attachments/" + usuarioId + "/" + noteId + "/" + UUID.randomUUID();
        storageService.put(key, new ByteArrayInputStream(bytes), bytes.length, detected);
        try {
            Adjunto adjunto = new Adjunto();
            adjunto.setNota(nota);
            adjunto.setUsuario(nota.getUsuario());
            adjunto.setNombreOriginal(original);
            adjunto.setStorageKey(key);
            adjunto.setMimeType(detected);
            adjunto.setTamanoBytes(bytes.length);
            adjunto.setChecksumSha256(sha256(bytes));
            adjunto.setEstado("READY");
            adjunto.setScanStatus(antivirusProperties.isEnabled() ? "PENDING" : "READY");
            adjunto.setCreatedAt(Instant.now());
            Adjunto saved = adjuntoRepository.save(adjunto);
            auditService.log("ATTACHMENT_UPLOAD", usuarioId, "ADJUNTO", saved.getId(), ip, userAgent);
            return mapper.toResponse(saved, downloadUrl(saved));
        } catch (RuntimeException ex) {
            storageService.delete(key);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> list(Long noteId, Long usuarioId) {
        notaRepository.findByIdAndUsuarioIdAndDeletedAtIsNull(noteId, usuarioId)
                .orElseThrow(() -> new NotFoundException("Nota no encontrada"));
        return adjuntoRepository.findAllByNotaIdAndUsuarioIdAndDeletedAtIsNull(noteId, usuarioId)
                .stream()
                .map(a -> mapper.toResponse(a, downloadUrl(a)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Download download(Long id, Long usuarioId) {
        Adjunto adjunto = adjuntoRepository.findByIdAndUsuarioIdAndDeletedAtIsNull(id, usuarioId)
                .orElseThrow(() -> new NotFoundException("Adjunto no encontrado"));
        if ("INFECTED".equals(adjunto.getScanStatus())) {
            throw new GoneException("El archivo fue bloqueado por el antivirus");
        }
        if (adjunto.getScanStatus() != null && !"READY".equals(adjunto.getScanStatus())) {
            throw new ConflictException("ATTACHMENT_NOT_READY", "El archivo todavía se está escaneando");
        }
        String url = storageService.presignedGetUrl(adjunto.getStorageKey(), properties.getPresignTtl());
        if (url != null) {
            return new Download(url, null, adjunto.getNombreOriginal(), adjunto.getMimeType());
        }
        return new Download(null, storageService.get(adjunto.getStorageKey()),
                adjunto.getNombreOriginal(), adjunto.getMimeType());
    }

    @Override
    @Transactional
    public void delete(Long id, Long usuarioId) {
        Adjunto adjunto = adjuntoRepository.findByIdAndUsuarioIdAndDeletedAtIsNull(id, usuarioId)
                .orElseThrow(() -> new NotFoundException("Adjunto no encontrado"));
        adjunto.setDeletedAt(Instant.now());
        adjuntoRepository.save(adjunto);
        storageService.delete(adjunto.getStorageKey());
        auditService.log("ATTACHMENT_DELETE", usuarioId, "ADJUNTO", id, null, null);
    }

    private String downloadUrl(Adjunto adjunto) {
        return "/api/v1/attachments/" + adjunto.getId() + "/download";
    }

    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            return "archivo";
        }
        String base = Path.of(name).getFileName().toString().replaceAll("[\\r\\n\\t]", "_");
        return base.length() <= 255 ? base : base.substring(base.length() - 255);
    }

    private String detect(byte[] bytes, String name) {
        try {
            return tika.detect(bytes, name);
        } catch (Exception ex) {
            throw new UnprocessableException("INVALID_FILE", "No se pudo determinar el tipo de archivo");
        }
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
