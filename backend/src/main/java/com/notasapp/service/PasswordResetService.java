package com.notasapp.service;

public interface PasswordResetService {
    void request(String email);

    void reset(String rawToken, String newPassword);
}
