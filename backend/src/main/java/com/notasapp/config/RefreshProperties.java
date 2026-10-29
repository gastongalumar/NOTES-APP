package com.notasapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.refresh")
@Getter
@Setter
public class RefreshProperties {
    private Duration ttl = Duration.ofDays(30);
    private String cookieName = "notasapp_rt";
    private boolean secure = false;
    private int maxFamilies = 5;
}
