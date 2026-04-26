package com.mocara.backend.auth.security;

import com.mocara.backend.auth.entity.AuthRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        if (properties.secret() == null || properties.secret().isBlank()) {
            throw new IllegalStateException("JWT secret must be provided via environment variable");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(properties.secret());
        } catch (IllegalArgumentException ex) {
            keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenPayload generateAccessToken(AuthenticatedUser user) {
        return generateToken(user, properties.accessTokenExpirationSeconds(), "access");
    }

    public TokenPayload generateRefreshToken(AuthenticatedUser user) {
        return generateToken(user, properties.refreshTokenExpirationSeconds(), "refresh");
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthenticatedUser toAuthenticatedUser(Claims claims) {
        Long userId = claims.get("userId", Long.class);
        String email = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> roleNames = claims.get("roles", List.class);
        Set<AuthRole> roles = roleNames.stream().map(AuthRole::valueOf).collect(Collectors.toSet());
        return new AuthenticatedUser(userId, email, roles);
    }

    private TokenPayload generateToken(AuthenticatedUser user, long expirySeconds, String tokenType) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirySeconds);
        String token = Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.email())
                .claim("type", tokenType)
                .claim("userId", user.userId())
                .claim("roles", user.roles().stream().map(Enum::name).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new TokenPayload(token, expiresAt.toEpochMilli());
    }

    public record TokenPayload(String token, long expiresAtMs) {}
}
