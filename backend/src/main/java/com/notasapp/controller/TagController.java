package com.notasapp.controller;

import com.notasapp.dto.tag.TagRequest;
import com.notasapp.dto.tag.TagResponse;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagResponse>> list(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(tagService.list(principal.userId()));
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(@AuthenticationPrincipal JwtPrincipal principal,
                                              @Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.create(principal.userId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(@AuthenticationPrincipal JwtPrincipal principal,
                                              @PathVariable Long id,
                                              @Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.update(id, principal.userId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal,
                                       @PathVariable Long id) {
        tagService.delete(id, principal.userId());
        return ResponseEntity.noContent().build();
    }
}
