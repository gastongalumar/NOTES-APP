package com.notasapp.service;

import com.notasapp.model.RefreshToken;
import com.notasapp.model.Usuario;

public interface RefreshTokenService {
    String issue(Usuario usuario, String userAgent, String ip);

    Rotation rotate(String rawToken, String userAgent, String ip);

    void revoke(String rawToken);

    void revokeAll(Usuario usuario);

    record Rotation(Usuario usuario, String rawToken) {
    }
}
