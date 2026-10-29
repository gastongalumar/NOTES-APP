package com.notasapp.service;

public interface EmailVerificationService {
    void request(Long usuarioId);

    void verify(String rawToken);
}
