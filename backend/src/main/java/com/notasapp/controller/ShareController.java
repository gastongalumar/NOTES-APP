package com.notasapp.controller;

import com.notasapp.dto.note.NoteResponse;
import com.notasapp.dto.share.ShareRequest;
import com.notasapp.dto.share.ShareResponse;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/notes/{noteId}/shares")
    public ResponseEntity<ShareResponse> create(@AuthenticationPrincipal JwtPrincipal principal,
                                                 @PathVariable Long noteId,
                                                 @RequestBody(required = false) ShareRequest request) {
        return ResponseEntity.ok(shareService.create(noteId, principal.userId(), request));
    }

    @GetMapping("/public/shares/{token}")
    public ResponseEntity<NoteResponse> publicNote(@PathVariable String token) {
        return ResponseEntity.ok(shareService.publicNote(token));
    }
}
