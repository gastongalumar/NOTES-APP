package com.notasapp.controller;

import com.notasapp.dto.auth.*;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> update(@AuthenticationPrincipal JwtPrincipal principal,
                                               @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(principal, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> password(@AuthenticationPrincipal JwtPrincipal principal,
                                         @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal,
                                       @Valid @RequestBody DeleteAccountRequest request) {
        authService.deleteAccount(principal, request);
        return ResponseEntity.noContent().build();
    }
}
