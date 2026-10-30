package com.notasapp.dto.auth;

public record TwoFactorStatusResponse(boolean enabled, int recoveryCodesRemaining) {
}
