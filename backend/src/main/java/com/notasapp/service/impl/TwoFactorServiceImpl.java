package com.notasapp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notasapp.dto.auth.TwoFactorSetupResponse;
import com.notasapp.exception.NotFoundException;
import com.notasapp.exception.UnprocessableException;
import com.notasapp.model.TwoFactor;
import com.notasapp.model.Usuario;
import com.notasapp.repository.TwoFactorRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.security.SecretCipher;
import com.notasapp.service.TwoFactorService;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TwoFactorServiceImpl implements TwoFactorService {

    private final TwoFactorRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final SecretCipher cipher;
    private final ObjectMapper objectMapper;
    private final SecureRandom random = new SecureRandom();
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();

    @Override
    @Transactional
    public TwoFactorSetupResponse setup(Long usuarioId) {
        Usuario usuario = findUser(usuarioId);
        String secret = secretGenerator.generate();
        TwoFactor twoFactor = repository.findById(usuarioId).orElseGet(() -> {
            TwoFactor created = new TwoFactor();
            created.setUsuario(usuario);
            return created;
        });
        twoFactor.setSecretEncrypted(cipher.encrypt(secret));
        twoFactor.setEnabledAt(null);
        repository.save(twoFactor);
        String label = URLEncoder.encode("NotasApp:" + usuario.getUsername(), StandardCharsets.UTF_8);
        String uri = "otpauth://totp/" + label + "?secret=" + secret + "&issuer=NotasApp";
        return new TwoFactorSetupResponse(secret, uri);
    }

    @Override
    @Transactional
    public List<String> verifyAndEnable(Long usuarioId, String code) {
        TwoFactor twoFactor = repository.findById(usuarioId)
                .orElseThrow(() -> new UnprocessableException("TWO_FACTOR_NOT_SETUP", "Primero iniciá la configuración de 2FA"));
        if (!verify(twoFactor, code)) {
            throw new UnprocessableException("INVALID_TOTP", "Código 2FA inválido");
        }
        List<String> codes = generateRecoveryCodes();
        twoFactor.setRecoveryCodesHash(hashCodes(codes));
        twoFactor.setEnabledAt(Instant.now());
        repository.save(twoFactor);
        return codes;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnabled(Long usuarioId) {
        return repository.findById(usuarioId)
                .map(twoFactor -> twoFactor.getEnabledAt() != null)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyLogin(Long usuarioId, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return repository.findById(usuarioId)
                .map(twoFactor -> authenticate(twoFactor, code))
                .orElse(false);
    }

    private boolean authenticate(TwoFactor twoFactor, String code) {
        if (verify(twoFactor, code)) {
            return true;
        }
        return matchesRecoveryCode(twoFactor, code);
    }

    private boolean verify(TwoFactor twoFactor, String code) {
        try {
            String secret = cipher.decrypt(twoFactor.getSecretEncrypted());
            CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
            return verifier.isValidCode(secret, code);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private boolean matchesRecoveryCode(TwoFactor twoFactor, String code) {
        if (twoFactor.getRecoveryCodesHash() == null) {
            return false;
        }
        try {
            List<String> hashes = objectMapper.readValue(twoFactor.getRecoveryCodesHash(), List.class);
            return hashes.contains(sha256(code));
        } catch (Exception ex) {
            return false;
        }
    }

    private List<String> generateRecoveryCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            codes.add(String.format("%08d", random.nextInt(100_000_000)));
        }
        return codes;
    }

    private String hashCodes(List<String> codes) {
        try {
            return objectMapper.writeValueAsString(codes.stream().map(this::sha256).toList());
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron generar los códigos de recuperación", ex);
        }
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    private Usuario findUser(Long usuarioId) {
        return usuarioRepository.findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }
}
