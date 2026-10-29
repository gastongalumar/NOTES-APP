package com.notasapp.service;

import com.notasapp.dto.attachment.AttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface AttachmentService {

    AttachmentResponse upload(Long noteId, Long usuarioId, MultipartFile file, String ip, String userAgent);

    List<AttachmentResponse> list(Long noteId, Long usuarioId);

    Download download(Long id, Long usuarioId);

    void delete(Long id, Long usuarioId);

    record Download(String url, InputStream stream, String filename, String mimeType) {
    }
}
