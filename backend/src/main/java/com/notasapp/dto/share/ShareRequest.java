package com.notasapp.dto.share;

import java.time.Instant;

public record ShareRequest(Instant expiresAt) {
}
