package com.notasapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.antivirus")
@Getter
@Setter
public class AntivirusProperties {
    private boolean enabled = false;
    private String host = "localhost";
    private int port = 3310;
    private Duration timeout = Duration.ofSeconds(30);
}
