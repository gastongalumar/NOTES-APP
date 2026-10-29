package com.notasapp.service.impl;

import com.notasapp.config.MailProperties;
import com.notasapp.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "mailhog", matchIfMissing = true)
public class MailHogMailService implements MailService {

    private final JavaMailSender javaMailSender;
    private final MailProperties properties;

    @Override
    public void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }
}
