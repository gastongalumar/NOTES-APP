package com.notasapp;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SeedPasswordTest {

    private static final String ADMIN_HASH = "$2b$10$FMe70a5m5tW4vPVOqiQNl.k1pkZ.D9NistcZwk9jEIpq3h6ypqFna";
    private static final String USER_HASH = "$2b$10$9vtU.vlDNpFlsWVlLbYkHuLq5NBdnSwW2W5Sb9g5yam491rRFMJxG";

    @Test
    void seedHashesMatchDemoPasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(encoder.matches("admin123", ADMIN_HASH)).isTrue();
        assertThat(encoder.matches("usuario123", USER_HASH)).isTrue();
    }
}
