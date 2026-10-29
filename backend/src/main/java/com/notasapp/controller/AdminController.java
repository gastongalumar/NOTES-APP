package com.notasapp.controller;

import com.notasapp.dto.PageResponse;
import com.notasapp.dto.admin.*;
import com.notasapp.dto.auth.UserResponse;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserResponse>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(adminService.users(q, pageable(page, size, "createdAt")));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> user(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.user(id));
    }

    @PatchMapping("/users/{id}/enabled")
    public ResponseEntity<UserResponse> enabled(@AuthenticationPrincipal JwtPrincipal principal,
                                                @PathVariable Long id,
                                                @Valid @RequestBody AdminUserEnabledRequest request) {
        return ResponseEntity.ok(adminService.setEnabled(id, request.enabled(), principal.userId()));
    }

    @PatchMapping("/users/{id}/roles")
    public ResponseEntity<UserResponse> roles(@AuthenticationPrincipal JwtPrincipal principal,
                                              @PathVariable Long id,
                                              @Valid @RequestBody AdminUserRolesRequest request) {
        return ResponseEntity.ok(adminService.setRoles(id, request.roles(), principal.userId()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal,
                                       @PathVariable Long id) {
        adminService.deleteUser(id, principal.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit")
    public ResponseEntity<PageResponse<AuditResponse>> audit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta) {
        return ResponseEntity.ok(adminService.audit(usuarioId, accion, desde, hasta,
                pageable(page, size, "createdAt")));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> stats() {
        return ResponseEntity.ok(adminService.stats());
    }

    private PageRequest pageable(int page, int size, String sort) {
        return PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Direction.DESC, sort));
    }
}
