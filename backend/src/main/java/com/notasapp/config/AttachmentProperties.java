package com.notasapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.attachments")
@Getter
@Setter
public class AttachmentProperties {
    private String provider = "local";
    private String localPath = "./storage";
    private long maxFileBytes = 10L * 1024 * 1024;
    private long maxTotalBytes = 200L * 1024 * 1024;
    private long maxPerNoteBytes = 50L * 1024 * 1024;
    private Duration presignTtl = Duration.ofMinutes(1);
}
