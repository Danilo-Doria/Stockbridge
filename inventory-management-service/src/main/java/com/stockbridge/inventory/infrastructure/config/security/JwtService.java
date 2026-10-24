package com.stockbridge.inventory.infrastructure.config.security;

import com.stockbridge.inventory.application.common.AccessToken;
import com.stockbridge.inventory.application.port.out.TokenProviderPort;
import com.stockbridge.inventory.domain.model.Role;
import com.stockbridge.inventory.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtService implements TokenProviderPort {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = props.expirationMinutes();
    }

    @Override
    public AccessToken issue(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)                    // HS256 por el tamaño de la clave
                .compact();
        return new AccessToken(token, expirationMinutes * 60);
    }

    /**
     * Lanza JwtException si la firma es inválida, el token está vencido o mal formado.
     */
    public AuthenticatedUser parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return new AuthenticatedUser(Long.valueOf(c.getSubject()),
                c.get("email", String.class),
                Role.valueOf(c.get("role", String.class)));
    }
}
