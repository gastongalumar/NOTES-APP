package com.notasapp.dto.auth;

public record TwoFactorSetupResponse(String secret, String otpauthUri) {
}
