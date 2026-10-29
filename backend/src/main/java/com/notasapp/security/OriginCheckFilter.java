package com.notasapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notasapp.config.CorsProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class OriginCheckFilter extends OncePerRequestFilter {

    private final CorsProperties corsProperties;
    private final ObjectMapper objectMapper;

    public OriginCheckFilter(CorsProperties corsProperties, ObjectMapper objectMapper) {
        this.corsProperties = corsProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !("/api/v1/auth/refresh".equals(path) || "/api/v1/auth/logout".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        if (origin != null && !corsProperties.getAllowedOrigins().contains(origin)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "status", 403,
                    "code", "FORBIDDEN",
                    "title", "Origen no permitido",
                    "detail", "El origen de la solicitud no está permitido"
            ));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
