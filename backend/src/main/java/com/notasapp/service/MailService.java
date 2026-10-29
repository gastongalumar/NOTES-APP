package com.notasapp.service;

public interface MailService {
    void send(String to, String subject, String text);
}
