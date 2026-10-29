package com.notasapp.security;

import com.notasapp.config.RefreshProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieFactory {

    private final RefreshProperties properties;

    public CookieFactory(RefreshProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie refresh(String value) {
        return base(value).maxAge(properties.getTtl()).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(properties.getCookieName(), value)
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite("Strict")
                .path("/");
    }
}
