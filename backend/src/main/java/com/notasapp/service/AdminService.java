package com.notasapp.service;

import com.notasapp.dto.PageResponse;
import com.notasapp.dto.admin.AdminStatsResponse;
import com.notasapp.dto.admin.AuditResponse;
import com.notasapp.dto.auth.UserResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Set;

public interface AdminService {

    PageResponse<UserResponse> users(String q, Pageable pageable);

    UserResponse user(Long id);

    UserResponse setEnabled(Long id, boolean enabled, Long adminId);

    UserResponse setRoles(Long id, Set<String> roles, Long adminId);

    void deleteUser(Long id, Long adminId);

    PageResponse<AuditResponse> audit(Long usuarioId, String accion, Instant desde, Instant hasta, Pageable pageable);

    AdminStatsResponse stats();
}
