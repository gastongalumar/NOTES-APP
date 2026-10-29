package com.notasapp.controller;

import com.notasapp.dto.PageResponse;
import com.notasapp.dto.note.*;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private static final Set<String> SORT_FIELDS = Set.of("updatedAt", "createdAt", "titulo", "fechaVencimiento", "prioridad");

    private final NoteService noteService;

    @GetMapping
    public ResponseEntity<PageResponse<NoteSummaryResponse>> list(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        return ResponseEntity.ok(noteService.list(principal.userId(), archived, q, categoryId, tagId,
                pageable(page, size, sort)));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<NoteSummaryResponse>> search(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(noteService.list(principal.userId(), null, q, null, null,
                pageable(page, size, "updatedAt,desc")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> get(@AuthenticationPrincipal JwtPrincipal principal,
                                            @PathVariable Long id) {
        return ResponseEntity.ok(noteService.get(id, principal.userId()));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@AuthenticationPrincipal JwtPrincipal principal,
                                               @Valid @RequestBody NoteCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.create(principal.userId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> update(@AuthenticationPrincipal JwtPrincipal principal,
                                               @PathVariable Long id,
                                               @Valid @RequestBody NoteUpdateRequest request) {
        return ResponseEntity.ok(noteService.update(id, principal.userId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal,
                                       @PathVariable Long id) {
        noteService.delete(id, principal.userId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<NoteResponse> archive(@AuthenticationPrincipal JwtPrincipal principal,
                                                @PathVariable Long id) {
        return ResponseEntity.ok(noteService.archive(id, principal.userId()));
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<NoteResponse> unarchive(@AuthenticationPrincipal JwtPrincipal principal,
                                                  @PathVariable Long id) {
        return ResponseEntity.ok(noteService.unarchive(id, principal.userId()));
    }

    @PatchMapping("/{id}/agenda")
    public ResponseEntity<NoteResponse> agenda(@AuthenticationPrincipal JwtPrincipal principal,
                                               @PathVariable Long id,
                                               @Valid @RequestBody AgendaUpdateRequest request) {
        return ResponseEntity.ok(noteService.updateAgenda(id, principal.userId(), request));
    }

    @GetMapping("/agenda")
    public ResponseEntity<List<NoteSummaryResponse>> agendaRange(@AuthenticationPrincipal JwtPrincipal principal,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(noteService.agenda(principal.userId(), from, to));
    }

    @GetMapping("/today")
    public ResponseEntity<List<NoteSummaryResponse>> today(@AuthenticationPrincipal JwtPrincipal principal) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant from = today.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1);
        return ResponseEntity.ok(noteService.agenda(principal.userId(), from, to));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<NoteSummaryResponse>> upcoming(@AuthenticationPrincipal JwtPrincipal principal,
                                                              @RequestParam(defaultValue = "7") int days) {
        int safeDays = Math.max(1, Math.min(days, 30));
        Instant now = Instant.now();
        return ResponseEntity.ok(noteService.agenda(principal.userId(), now, now.plusSeconds(safeDays * 86400L)));
    }

    private Pageable pageable(int page, int size, String sort) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        String[] parts = sort == null ? new String[]{"updatedAt", "desc"} : sort.split(",", 2);
        String field = SORT_FIELDS.contains(parts[0]) ? parts[0] : "updatedAt";
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, field));
    }
}
