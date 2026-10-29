package com.notasapp.service.impl;

import com.notasapp.config.MailProperties;
import com.notasapp.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "resend")
public class ResendMailService implements MailService {

    private final MailProperties properties;
    private final RestClient restClient = RestClient.create();

    @Override
    public void send(String to, String subject, String text) {
        if (properties.getResendApiKey() == null || properties.getResendApiKey().isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY es obligatorio con MAIL_PROVIDER=resend");
        }
        restClient.post()
                .uri("https://api.resend.com/emails")
                .header("Authorization", "Bearer " + properties.getResendApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", properties.getFrom(),
                        "to", List.of(to),
                        "subject", subject,
                        "text", text
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
