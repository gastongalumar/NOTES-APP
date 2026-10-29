package com.notasapp.service.impl;

import com.notasapp.dto.PageResponse;
import com.notasapp.dto.admin.AdminStatsResponse;
import com.notasapp.dto.admin.AuditResponse;
import com.notasapp.dto.auth.UserResponse;
import com.notasapp.exception.ConflictException;
import com.notasapp.exception.NotFoundException;
import com.notasapp.exception.UnprocessableException;
import com.notasapp.mapper.UserMapper;
import com.notasapp.model.AuditLog;
import com.notasapp.model.Role;
import com.notasapp.model.Usuario;
import com.notasapp.repository.*;
import com.notasapp.service.AdminService;
import com.notasapp.service.AuditService;
import com.notasapp.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UsuarioRepository usuarioRepository;
    private final NotaRepository notaRepository;
    private final AdjuntoRepository adjuntoRepository;
    private final AuditLogRepository auditLogRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> users(String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return PageResponse.from(usuarioRepository.search(query, pageable), userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse user(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled, Long adminId) {
        Usuario usuario = findUser(id);
        if (id.equals(adminId) && !enabled) {
            throw new ConflictException("SELF_DISABLE", "No podés deshabilitar tu propia cuenta");
        }
        if (!enabled && hasAdmin(usuario) && usuarioRepository.countActiveAdmins() <= 1) {
            throw new ConflictException("LAST_ADMIN", "No podés deshabilitar al último administrador");
        }
        usuario.setEnabled(enabled);
        if (!enabled) {
            refreshTokenService.revokeAll(usuario);
        }
        Usuario saved = usuarioRepository.save(usuario);
        auditService.log(enabled ? "ADMIN_USER_ENABLE" : "ADMIN_USER_DISABLE",
                adminId, "USUARIO", id, null, null);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse setRoles(Long id, Set<String> roleNames, Long adminId) {
        Usuario usuario = findUser(id);
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByNombre(roleName.toUpperCase())
                    .orElseThrow(() -> new UnprocessableException("INVALID_ROLE", "Rol inválido: " + roleName));
            roles.add(role);
        }
        boolean removesAdmin = hasAdmin(usuario) && roles.stream().noneMatch(r -> "ADMIN".equals(r.getNombre()));
        if (removesAdmin && (id.equals(adminId) || usuarioRepository.countActiveAdmins() <= 1)) {
            throw new ConflictException("LAST_ADMIN", "No podés quitar el último rol ADMIN");
        }
        usuario.setRoles(roles);
        Usuario saved = usuarioRepository.save(usuario);
        auditService.log("ADMIN_ROLE_CHANGE", adminId, "USUARIO", id, null, null);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, Long adminId) {
        Usuario usuario = findUser(id);
        if (id.equals(adminId)) {
            throw new ConflictException("SELF_DELETE", "No podés eliminar tu propia cuenta");
        }
        if (hasAdmin(usuario) && usuarioRepository.countActiveAdmins() <= 1) {
            throw new ConflictException("LAST_ADMIN", "No podés eliminar al último administrador");
        }
        refreshTokenService.revokeAll(usuario);
        usuario.setEnabled(false);
        usuario.setDeletedAt(Instant.now());
        usuario.setUsername("deleted_" + usuario.getId() + "_" + usuario.getUsername());
        usuario.setEmail("deleted_" + usuario.getId() + "@invalid.local");
        usuarioRepository.save(usuario);
        auditService.log("ADMIN_USER_DELETE", adminId, "USUARIO", id, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditResponse> audit(Long usuarioId, String accion, Instant desde, Instant hasta,
                                             Pageable pageable) {
        String action = (accion == null || accion.isBlank()) ? null : accion.trim();
        Page<AuditLog> page = auditLogRepository.search(usuarioId, action, desde, hasta, pageable);
        return PageResponse.from(page, this::toAudit);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse stats() {
        return new AdminStatsResponse(
                usuarioRepository.countByDeletedAtIsNull(),
                notaRepository.countByDeletedAtIsNull(),
                notaRepository.countByDeletedAtIsNullAndArchivadaTrue(),
                adjuntoRepository.countByDeletedAtIsNull(),
                auditLogRepository.countByAccion("LOGIN_SUCCESS"),
                auditLogRepository.countByAccion("LOGIN_FAILED")
        );
    }

    private AuditResponse toAudit(AuditLog log) {
        return new AuditResponse(
                log.getId(),
                log.getUsuario() == null ? null : log.getUsuario().getId(),
                log.getUsuario() == null ? null : log.getUsuario().getUsername(),
                log.getAccion(),
                log.getEntidad(),
                log.getEntidadId(),
                log.getIp(),
                log.getUserAgent(),
                log.getCreatedAt()
        );
    }

    private Usuario findUser(Long id) {
        return usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private boolean hasAdmin(Usuario usuario) {
        return usuario.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getNombre()));
    }
}
