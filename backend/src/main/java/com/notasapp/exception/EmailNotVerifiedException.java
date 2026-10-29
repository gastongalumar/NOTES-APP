package com.notasapp.exception;

import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends ApiException {
    public EmailNotVerifiedException(String email) {
        super(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED",
                "Verificá tu email antes de iniciar sesión: " + email);
    }
}
