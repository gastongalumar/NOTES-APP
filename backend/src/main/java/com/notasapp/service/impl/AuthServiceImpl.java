package com.notasapp.service.impl;

import com.notasapp.config.AuthProperties;
import com.notasapp.dto.auth.*;
import com.notasapp.exception.AccountLockedException;
import com.notasapp.exception.ConflictException;
import com.notasapp.exception.EmailNotVerifiedException;
import com.notasapp.exception.UnauthorizedException;
import com.notasapp.mapper.UserMapper;
import com.notasapp.model.Role;
import com.notasapp.model.Usuario;
import com.notasapp.repository.RoleRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.security.JwtService;
import com.notasapp.service.AuditService;
import com.notasapp.service.AuthService;
import com.notasapp.service.EmailVerificationService;
import com.notasapp.service.RefreshTokenService;
import com.notasapp.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DUMMY_HASH = "$2b$10$FMe70a5m5tW4vPVOqiQNl.k1pkZ.D9NistcZwk9jEIpq3h6ypqFna";

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final AuthProperties authProperties;
    private final AuditService auditService;
    private final TwoFactorService twoFactorService;
    private final EmailVerificationService emailVerificationService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByUsernameAndDeletedAtIsNull(username)) {
            throw new ConflictException("USERNAME_TAKEN", "El nombre de usuario ya está en uso");
        }
        if (usuarioRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new ConflictException("EMAIL_TAKEN", "El email ya está registrado");
        }
        Role role = roleRepository.findByNombre("USER")
                .orElseThrow(() -> new IllegalStateException("Falta el rol USER"));
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setNombre(request.nombre() == null ? null : request.nombre().trim());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setEnabled(true);
        usuario.getRoles().add(role);
        Usuario saved = usuarioRepository.save(usuario);
        emailVerificationService.request(saved.getId());
        auditService.log("REGISTER", saved.getId(), "USUARIO", saved.getId(), null, null);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthResult login(LoginRequest request, String ip, String userAgent) {
        Usuario usuario = usuarioRepository.findByUsernameAndDeletedAtIsNull(request.username().trim()).orElse(null);
        if (usuario == null) {
            passwordEncoder.matches(request.password(), DUMMY_HASH);
            auditService.log("LOGIN_FAILED", null, "USUARIO", null, ip, userAgent);
            throw new UnauthorizedException("Credenciales inválidas");
        }
        if (!usuario.isEnabled()) {
            auditService.log("LOGIN_FAILED", usuario.getId(), "USUARIO", usuario.getId(), ip, userAgent);
            throw new UnauthorizedException("Credenciales inválidas");
        }
        if (usuario.isLocked()) {
            throw new AccountLockedException("La cuenta está bloqueada temporalmente");
        }
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            int attempts = usuario.getFailedAttempts() + 1;
            usuario.setFailedAttempts(attempts);
            if (attempts >= authProperties.getMaxFailedAttempts()) {
                usuario.setLockedUntil(Instant.now().plus(authProperties.getLockDuration()));
                usuario.setFailedAttempts(0);
            }
            usuarioRepository.save(usuario);
            auditService.log("LOGIN_FAILED", usuario.getId(), "USUARIO", usuario.getId(), ip, userAgent);
            throw new UnauthorizedException("Credenciales inválidas");
        }
        if (!usuario.isEmailVerified()) {
            throw new EmailNotVerifiedException(usuario.getEmail());
        }
        if (twoFactorService.isEnabled(usuario.getId())
                && !twoFactorService.verifyLogin(usuario.getId(), request.totp())) {
            auditService.log("LOGIN_2FA_FAILED", usuario.getId(), "USUARIO", usuario.getId(), ip, userAgent);
            throw new UnauthorizedException("Código 2FA inválido");
        }
        usuario.setFailedAttempts(0);
        usuario.setLockedUntil(null);
        usuarioRepository.save(usuario);
        String access = jwtService.generateAccessToken(usuario);
        String refresh = refreshTokenService.issue(usuario, userAgent, ip);
        auditService.log("LOGIN_SUCCESS", usuario.getId(), "USUARIO", usuario.getId(), ip, userAgent);
        return new AuthResult(access, refresh, jwtService.accessTtlSeconds(), userMapper.toResponse(usuario));
    }

    @Override
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthResult refresh(String rawToken, String ip, String userAgent) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException("Refresh token ausente");
        }
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(rawToken, userAgent, ip);
        String access = jwtService.generateAccessToken(rotation.usuario());
        return new AuthResult(access, rotation.rawToken(), jwtService.accessTtlSeconds(),
                userMapper.toResponse(rotation.usuario()));
    }

    @Override
    public void logout(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            refreshTokenService.revoke(rawToken);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(JwtPrincipal principal) {
        Usuario usuario = findUser(principal.userId());
        return userMapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(JwtPrincipal principal, UpdateProfileRequest request) {
        Usuario usuario = findUser(principal.userId());
        if (request.email() != null && !request.email().isBlank()
                && !request.email().trim().equalsIgnoreCase(usuario.getEmail())) {
            String email = request.email().trim().toLowerCase();
            if (usuarioRepository.existsByEmailAndDeletedAtIsNull(email)) {
                throw new ConflictException("EMAIL_TAKEN", "El email ya está registrado");
            }
            usuario.setEmail(email);
        }
        if (request.nombre() != null) {
            usuario.setNombre(request.nombre().trim());
        }
        return userMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void changePassword(JwtPrincipal principal, ChangePasswordRequest request) {
        Usuario usuario = findUser(principal.userId());
        if (!passwordEncoder.matches(request.currentPassword(), usuario.getPasswordHash())) {
            throw new UnauthorizedException("La contraseña actual no es correcta");
        }
        usuario.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        usuarioRepository.save(usuario);
        refreshTokenService.revokeAll(usuario);
        auditService.log("PASSWORD_CHANGED", usuario.getId(), "USUARIO", usuario.getId(), null, null);
    }

    @Override
    @Transactional
    public void deleteAccount(JwtPrincipal principal, DeleteAccountRequest request) {
        Usuario usuario = findUser(principal.userId());
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new UnauthorizedException("La contraseña no es correcta");
        }
        refreshTokenService.revokeAll(usuario);
        usuario.setDeletedAt(Instant.now());
        usuario.setEnabled(false);
        usuario.setUsername("deleted_" + usuario.getId() + "_" + usuario.getUsername());
        usuario.setEmail("deleted_" + usuario.getId() + "@invalid.local");
        usuarioRepository.save(usuario);
        auditService.log("ACCOUNT_DELETED", usuario.getId(), "USUARIO", usuario.getId(), null, null);
    }

    private Usuario findUser(Long id) {
        return usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
    }
}
