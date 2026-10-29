package com.notasapp.service.impl;

import com.notasapp.config.RefreshProperties;
import com.notasapp.exception.UnauthorizedException;
import com.notasapp.model.RefreshToken;
import com.notasapp.model.Usuario;
import com.notasapp.repository.RefreshTokenRepository;
import com.notasapp.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final RefreshProperties properties;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public String issue(Usuario usuario, String userAgent, String ip) {
        Instant now = Instant.now();
        long activeFamilies = repository.countActiveFamilies(usuario.getId(), now);
        if (activeFamilies >= properties.getMaxFamilies()) {
            repository.findActiveTokens(usuario.getId(), now).stream()
                    .findFirst()
                    .ifPresent(oldest -> repository.revokeFamily(oldest.getFamilyId(), now));
        }
        return create(usuario, UUID.randomUUID().toString(), userAgent, ip, now).raw;
    }

    @Override
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public Rotation rotate(String rawToken, String userAgent, String ip) {
        RefreshToken current = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));
        Instant now = Instant.now();
        if (current.getRevokedAt() != null) {
            repository.revokeFamily(current.getFamilyId(), now);
            throw new UnauthorizedException("Refresh token reutilizado");
        }
        if (current.getExpiresAt().isBefore(now)) {
            throw new UnauthorizedException("Refresh token expirado");
        }
        Issued next = create(current.getUsuario(), current.getFamilyId(), userAgent, ip, now);
        current.setRevokedAt(now);
        current.setReplacedBy(next.entity);
        repository.save(current);
        return new Rotation(current.getUsuario(), next.raw);
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> repository.revokeFamily(token.getFamilyId(), Instant.now()));
    }

    @Override
    @Transactional
    public void revokeAll(Usuario usuario) {
        repository.findActiveTokens(usuario.getId(), Instant.now())
                .forEach(token -> repository.revokeFamily(token.getFamilyId(), Instant.now()));
    }

    private Issued create(Usuario usuario, String familyId, String userAgent, String ip, Instant now) {
        String raw = randomToken();
        RefreshToken token = new RefreshToken();
        token.setUsuario(usuario);
        token.setTokenHash(hash(raw));
        token.setFamilyId(familyId);
        token.setIssuedAt(now);
        token.setExpiresAt(now.plus(properties.getTtl()));
        token.setUserAgent(trim(userAgent, 255));
        token.setIp(trim(ip, 45));
        token.setCreatedAt(now);
        return new Issued(raw, repository.save(token));
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    private String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private record Issued(String raw, RefreshToken entity) {
    }
}
