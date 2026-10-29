package com.notasapp.security;

import com.notasapp.config.JwtProperties;
import com.notasapp.model.Role;
import com.notasapp.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtProperties properties;
    private Key key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            throw new IllegalStateException("JWT_SECRET es obligatorio");
        }
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(properties.getSecret());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("JWT_SECRET debe estar en base64", ex);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 256 bits");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(Usuario usuario) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", usuario.getId());
        claims.put("username", usuario.getUsername());
        claims.put("roles", usuario.getRoles().stream().map(Role::getNombre).collect(Collectors.toSet()));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(usuario.getId()))
                .setIssuer(properties.getIssuer())
                .setAudience(properties.getAudience())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(properties.getAccessTtl())))
                .setId(UUID.randomUUID().toString())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public JwtPrincipal parse(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        if (!properties.getIssuer().equals(claims.getIssuer())
                || !properties.getAudience().equals(claims.getAudience())) {
            throw new IllegalArgumentException("Token de otro emisor o audiencia");
        }
        Long userId = ((Number) claims.get("uid")).longValue();
        String username = (String) claims.get("username");
        List<String> roles = (List<String>) claims.get("roles", List.class);
        return new JwtPrincipal(userId, username, Set.copyOf(roles));
    }

    public long accessTtlSeconds() {
        return properties.getAccessTtl().toSeconds();
    }
}
