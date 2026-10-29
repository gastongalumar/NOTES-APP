package com.notasapp.service.impl;

import com.notasapp.config.MailProperties;
import com.notasapp.exception.NotFoundException;
import com.notasapp.exception.UnprocessableException;
import com.notasapp.model.AccountToken;
import com.notasapp.model.Usuario;
import com.notasapp.repository.AccountTokenRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.service.EmailVerificationService;
import com.notasapp.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final String TYPE = "EMAIL_VERIFY";

    private final UsuarioRepository usuarioRepository;
    private final AccountTokenRepository tokenRepository;
    private final MailService mailService;
    private final MailProperties mailProperties;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public void request(Long usuarioId) {
        Usuario usuario = usuarioRepository.findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (usuario.isEmailVerified()) {
            return;
        }
        String raw = randomToken();
        AccountToken token = new AccountToken();
        token.setUsuario(usuario);
        token.setTipo(TYPE);
        token.setTokenHash(hash(raw));
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        tokenRepository.save(token);
        String link = mailProperties.getFrontendUrl() + "/verify-email?token=" + raw;
        try {
            mailService.send(usuario.getEmail(), "Verificá tu email de NotasApp",
                    "Confirmá tu email con este enlace:\n\n" + link);
        } catch (RuntimeException ex) {
            log.warn("No se pudo enviar verificación de email para usuario {}", usuarioId);
        }
    }

    @Override
    @Transactional
    public void verify(String rawToken) {
        AccountToken token = tokenRepository.findByTokenHashAndTipo(hash(rawToken), TYPE)
                .orElseThrow(() -> new UnprocessableException("INVALID_EMAIL_TOKEN", "El enlace no es válido"));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnprocessableException("EXPIRED_EMAIL_TOKEN", "El enlace expiró");
        }
        Usuario usuario = token.getUsuario();
        usuario.setEmailVerified(true);
        usuarioRepository.save(usuario);
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
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
