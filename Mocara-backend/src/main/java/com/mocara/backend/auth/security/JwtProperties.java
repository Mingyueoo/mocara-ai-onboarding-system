package com.mocara.backend.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenExpirationSeconds,
        long refreshTokenExpirationSeconds
) {}
