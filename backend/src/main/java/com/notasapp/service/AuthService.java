package com.notasapp.service;

import com.notasapp.dto.auth.*;
import com.notasapp.security.JwtPrincipal;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResult login(LoginRequest request, String ip, String userAgent);

    AuthResult refresh(String rawToken, String ip, String userAgent);

    void logout(String rawToken);

    UserResponse me(JwtPrincipal principal);

    UserResponse updateProfile(JwtPrincipal principal, UpdateProfileRequest request);

    void changePassword(JwtPrincipal principal, ChangePasswordRequest request);

    void deleteAccount(JwtPrincipal principal, DeleteAccountRequest request);

    record AuthResult(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
    }
}
