package com.notasapp.controller;

import com.notasapp.dto.attachment.AttachmentResponse;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.AttachmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/notes/{noteId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> upload(@AuthenticationPrincipal JwtPrincipal principal,
                                                     @PathVariable Long noteId,
                                                     @RequestPart("file") MultipartFile file,
                                                     HttpServletRequest request) {
        AttachmentResponse response = attachmentService.upload(
                noteId, principal.userId(), file, clientIp(request), request.getHeader("User-Agent"));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/notes/{noteId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> list(@AuthenticationPrincipal JwtPrincipal principal,
                                                         @PathVariable Long noteId) {
        return ResponseEntity.ok(attachmentService.list(noteId, principal.userId()));
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<?> download(@AuthenticationPrincipal JwtPrincipal principal,
                                      @PathVariable Long id) {
        AttachmentService.Download download = attachmentService.download(id, principal.userId());
        if (download.url() != null) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(download.url())).build();
        }
        InputStreamResource resource = new InputStreamResource(download.stream());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.filename(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(resource);
    }

    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal,
                                       @PathVariable Long id) {
        attachmentService.delete(id, principal.userId());
        return ResponseEntity.noContent().build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
