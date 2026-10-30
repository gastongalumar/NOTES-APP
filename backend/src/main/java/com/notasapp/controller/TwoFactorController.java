package com.notasapp.controller;

import com.notasapp.dto.auth.DisableTwoFactorRequest;
import com.notasapp.dto.auth.TwoFactorSetupResponse;
import com.notasapp.dto.auth.TwoFactorStatusResponse;
import com.notasapp.dto.auth.TwoFactorVerifyRequest;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.TwoFactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @GetMapping("/status")
    public ResponseEntity<TwoFactorStatusResponse> status(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(twoFactorService.status(principal.userId()));
    }

    @PostMapping("/setup")
    public ResponseEntity<TwoFactorSetupResponse> setup(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(twoFactorService.setup(principal.userId()));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, List<String>>> verify(@AuthenticationPrincipal JwtPrincipal principal,
                                                             @Valid @RequestBody TwoFactorVerifyRequest request) {
        return ResponseEntity.ok(Map.of("recoveryCodes",
                twoFactorService.verifyAndEnable(principal.userId(), request.code())));
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disable(@AuthenticationPrincipal JwtPrincipal principal,
                                        @Valid @RequestBody DisableTwoFactorRequest request) {
        twoFactorService.disable(principal.userId(), request.password());
        return ResponseEntity.noContent().build();
    }
}
