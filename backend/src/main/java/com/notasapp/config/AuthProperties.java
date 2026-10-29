package com.notasapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.auth")
@Getter
@Setter
public class AuthProperties {
    private int maxFailedAttempts = 5;
    private Duration lockDuration = Duration.ofMinutes(15);
    private int loginRateLimit = 5;
    private int registerRateLimit = 3;
    private int refreshRateLimit = 30;
}
