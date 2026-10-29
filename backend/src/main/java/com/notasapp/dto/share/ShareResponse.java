package com.notasapp.dto.share;

import java.time.Instant;

public record ShareResponse(String url, Instant expiresAt) {
}
