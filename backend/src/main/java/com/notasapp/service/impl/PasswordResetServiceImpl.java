package com.notasapp.service.impl;

import com.notasapp.config.MailProperties;
import com.notasapp.exception.UnprocessableException;
import com.notasapp.model.AccountToken;
import com.notasapp.model.Usuario;
import com.notasapp.repository.AccountTokenRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.service.AuditService;
import com.notasapp.service.MailService;
import com.notasapp.service.PasswordResetService;
import com.notasapp.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String TYPE = "PASSWORD_RESET";

    private final UsuarioRepository usuarioRepository;
    private final AccountTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final MailService mailService;
    private final MailProperties mailProperties;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public void request(String email) {
        usuarioRepository.findByEmailAndDeletedAtIsNull(email.trim().toLowerCase()).ifPresent(usuario -> {
            String raw = randomToken();
            AccountToken token = new AccountToken();
            token.setUsuario(usuario);
            token.setTipo(TYPE);
            token.setTokenHash(hash(raw));
            token.setCreatedAt(Instant.now());
            token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            tokenRepository.save(token);
            String link = mailProperties.getFrontendUrl() + "/reset-password?token=" + raw;
            try {
                mailService.send(usuario.getEmail(), "Restablecer contraseña de NotasApp",
                        "Usá este enlace durante los próximos 30 minutos:\n\n" + link);
            } catch (RuntimeException ex) {
                log.warn("No se pudo enviar el email de reset para el usuario {}", usuario.getId());
            }
            auditService.log("PASSWORD_RESET_REQUEST", usuario.getId(), "USUARIO", usuario.getId(), null, null);
        });
    }

    @Override
    @Transactional
    public void reset(String rawToken, String newPassword) {
        AccountToken token = tokenRepository.findByTokenHashAndTipo(hash(rawToken), TYPE)
                .orElseThrow(() -> new UnprocessableException("INVALID_RESET_TOKEN", "El enlace no es válido"));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnprocessableException("EXPIRED_RESET_TOKEN", "El enlace expiró");
        }
        Usuario usuario = token.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuario.setFailedAttempts(0);
        usuario.setLockedUntil(null);
        usuarioRepository.save(usuario);
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
        refreshTokenService.revokeAll(usuario);
        auditService.log("PASSWORD_RESET", usuario.getId(), "USUARIO", usuario.getId(), null, null);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
