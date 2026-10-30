package com.notasapp.service;

public interface EmailVerificationService {
    void request(Long usuarioId);

    void resend(String email);

    void verify(String rawToken);
}
