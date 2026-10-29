package com.notasapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.bootstrap-admin")
@Getter
@Setter
public class BootstrapAdminProperties {
    private String username;
    private String email;
    private String password;
}
