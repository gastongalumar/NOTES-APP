package com.notasapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmailNotVerifiedException extends ApiException {

    private final String email;

    public EmailNotVerifiedException(String email) {
        super(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED",
                "Verificá tu email antes de iniciar sesión: " + email);
        this.email = email;
    }
}
