package com.notasapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mail")
@Getter
@Setter
public class MailProperties {
    private String provider = "mailhog";
    private String from = "notasapp@local.test";
    private String frontendUrl = "http://localhost:5173";
    private String resendApiKey;
}
