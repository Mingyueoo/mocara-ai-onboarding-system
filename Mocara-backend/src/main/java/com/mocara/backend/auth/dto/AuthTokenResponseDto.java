package com.mocara.backend.auth.dto;

import java.util.Set;

public record AuthTokenResponseDto(
        String tokenType,
        String accessToken,
        long accessTokenExpiresAtMs,
        String refreshToken,
        long refreshTokenExpiresAtMs,
        long userId,
        String email,
        Set<String> roles
) {}
