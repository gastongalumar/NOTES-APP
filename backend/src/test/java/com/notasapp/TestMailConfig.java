package com.notasapp;

import com.notasapp.service.MailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestMailConfig {

    @Bean
    @Primary
    public MailService testMailService() {
        return (to, subject, text) -> {
        };
    }
}
