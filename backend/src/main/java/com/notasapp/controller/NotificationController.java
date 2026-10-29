package com.notasapp.controller;

import com.notasapp.dto.PageResponse;
import com.notasapp.dto.notification.NotificationResponse;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> list(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "false") boolean unread,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.list(
                principal.userId(), unread,
                PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)))));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unread(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(principal.userId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> read(@AuthenticationPrincipal JwtPrincipal principal,
                                     @PathVariable Long id) {
        notificationService.markRead(id, principal.userId());
        return ResponseEntity.noContent().build();
    }
}
