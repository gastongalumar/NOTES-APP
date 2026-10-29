package com.notasapp.controller;

import com.notasapp.dto.note.NoteResponse;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ExportController {

    private final NoteService noteService;

    @GetMapping(value = "/notes/{id}/export.md", produces = "text/markdown")
    public ResponseEntity<String> markdown(@AuthenticationPrincipal JwtPrincipal principal,
                                           @PathVariable Long id) {
        NoteResponse note = noteService.get(id, principal.userId());
        String body = "# " + note.titulo() + "\n\n" + (note.contenido() == null ? "" : note.contenido());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nota-" + id + ".md\"")
                .contentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"))
                .body(body);
    }
}
