package com.notasapp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notasapp.dto.auth.TwoFactorSetupResponse;
import com.notasapp.dto.auth.TwoFactorStatusResponse;
import com.notasapp.exception.ConflictException;
import com.notasapp.exception.NotFoundException;
import com.notasapp.exception.UnauthorizedException;
import com.notasapp.exception.UnprocessableException;
import com.notasapp.model.TwoFactor;
import com.notasapp.model.Usuario;
import com.notasapp.repository.TwoFactorRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.security.SecretCipher;
import com.notasapp.service.AuditService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();

    @Override
    @Transactional
    public TwoFactorSetupResponse setup(Long usuarioId) {
        Usuario usuario = findUser(usuarioId);
        TwoFactor twoFactor = repository.findById(usuarioId).orElseGet(() -> {
            TwoFactor created = new TwoFactor();
            created.setUsuario(usuario);
            return created;
        });
        if (twoFactor.getEnabledAt() != null) {
            throw new ConflictException("TWO_FACTOR_ALREADY_ENABLED",
                    "La autenticación en dos pasos ya está activa");
        }
        String secret = secretGenerator.generate();
        twoFactor.setSecretEncrypted(cipher.encrypt(secret));
        twoFactor.setEnabledAt(null);
        twoFactor.setRecoveryCodesHash(null);
        repository.save(twoFactor);
        String label = URLEncoder.encode("NotasApp:" + usuario.getUsername(), StandardCharsets.UTF_8);
        String uri = "otpauth://totp/" + label + "?secret=" + secret + "&issuer=NotasApp";
        return new TwoFactorSetupResponse(secret, uri);
    }

    @Override
    @Transactional
    public List<String> verifyAndEnable(Long usuarioId, String code) {
        TwoFactor twoFactor = repository.findById(usuarioId)
                .orElseThrow(() -> new UnprocessableException("TWO_FACTOR_NOT_SETUP",
                        "Primero iniciá la configuración de 2FA"));
        if (twoFactor.getEnabledAt() != null) {
            throw new ConflictException("TWO_FACTOR_ALREADY_ENABLED",
                    "La autenticación en dos pasos ya está activa");
        }
        if (!verify(twoFactor, code)) {
            throw new UnprocessableException("INVALID_TOTP", "Código 2FA inválido");
        }
        List<String> codes = generateRecoveryCodes();
        twoFactor.setRecoveryCodesHash(hashCodes(codes));
        twoFactor.setEnabledAt(Instant.now());
        repository.save(twoFactor);
        auditService.log("TWO_FACTOR_ENABLED", usuarioId, "USUARIO", usuarioId, null, null);
        return codes;
    }

    @Override
    @Transactional(readOnly = true)
    public TwoFactorStatusResponse status(Long usuarioId) {
        return repository.findById(usuarioId)
                .map(twoFactor -> new TwoFactorStatusResponse(
                        twoFactor.getEnabledAt() != null,
                        readHashes(twoFactor).size()))
                .orElseGet(() -> new TwoFactorStatusResponse(false, 0));
    }

    @Override
    @Transactional
    public void disable(Long usuarioId, String password) {
        Usuario usuario = findUser(usuarioId);
        TwoFactor twoFactor = repository.findById(usuarioId)
                .filter(stored -> stored.getEnabledAt() != null)
                .orElseThrow(() -> new UnprocessableException("TWO_FACTOR_NOT_ENABLED",
                        "La autenticación en dos pasos no está activa"));
        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new UnauthorizedException("La contraseña no es correcta");
        }
        repository.delete(twoFactor);
        auditService.log("TWO_FACTOR_DISABLED", usuarioId, "USUARIO", usuarioId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnabled(Long usuarioId) {
        return repository.findById(usuarioId)
                .map(twoFactor -> twoFactor.getEnabledAt() != null)
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean verifyLogin(Long usuarioId, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        TwoFactor twoFactor = repository.findById(usuarioId).orElse(null);
        if (twoFactor == null || twoFactor.getEnabledAt() == null) {
            return false;
        }
        if (verify(twoFactor, code.trim())) {
            return true;
        }
        return consumeRecoveryCode(twoFactor, code.trim());
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

    private boolean consumeRecoveryCode(TwoFactor twoFactor, String code) {
        List<String> hashes = readHashes(twoFactor);
        String target = sha256(code);
        int index = hashes.indexOf(target);
        if (index < 0) {
            return false;
        }
        hashes.remove(index);
        twoFactor.setRecoveryCodesHash(writeHashes(hashes));
        repository.save(twoFactor);
        auditService.log("TWO_FACTOR_RECOVERY_USED", twoFactor.getUsuarioId(), "USUARIO",
                twoFactor.getUsuarioId(), null, null);
        return true;
    }

    private List<String> readHashes(TwoFactor twoFactor) {
        if (twoFactor.getRecoveryCodesHash() == null) {
            return new ArrayList<>();
        }
        try {
            String[] stored = objectMapper.readValue(twoFactor.getRecoveryCodesHash(), String[].class);
            return new ArrayList<>(java.util.Arrays.asList(stored));
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private String writeHashes(List<String> hashes) {
        try {
            return objectMapper.writeValueAsString(hashes);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron actualizar los códigos de recuperación", ex);
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
        return writeHashes(codes.stream().map(this::sha256).toList());
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
