package com.notasapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private Duration accessTtl = Duration.ofMinutes(15);
    private String issuer = "notasapp";
    private String audience = "notasapp-web";
}
