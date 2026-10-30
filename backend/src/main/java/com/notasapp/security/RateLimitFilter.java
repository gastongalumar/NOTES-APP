package com.notasapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notasapp.config.AuthProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RESEND_EMAIL_PATH = "/api/v1/auth/email/resend";

    private final AuthProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(AuthProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/refresh")
                || path.equals(RESEND_EMAIL_PATH));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        int limit;
        Duration window;
        if (path.equals(RESEND_EMAIL_PATH)) {
            limit = properties.getEmailResendRateLimit();
            window = Duration.ofHours(1);
        } else if (path.endsWith("login")) {
            limit = properties.getLoginRateLimit();
            window = Duration.ofMinutes(1);
        } else if (path.endsWith("register")) {
            limit = properties.getRegisterRateLimit();
            window = Duration.ofHours(1);
        } else {
            limit = properties.getRefreshRateLimit();
            window = Duration.ofMinutes(1);
        }
        String key = clientIp(request) + ":" + path;
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> Bucket.builder()
                .addLimit(Bandwidth.classic(limit, Refill.greedy(limit, window)))
                .build());
        if (!bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(window.toSeconds()));
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "status", 429,
                    "code", "RATE_LIMITED",
                    "title", "Demasiadas solicitudes",
                    "detail", "Esperá antes de volver a intentar"
            ));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
