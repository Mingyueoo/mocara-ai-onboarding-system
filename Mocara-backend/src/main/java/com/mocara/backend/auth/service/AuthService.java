package com.mocara.backend.auth.service;

import com.mocara.backend.auth.dto.AuthTokenResponseDto;
import com.mocara.backend.auth.entity.AppUserEntity;
import com.mocara.backend.auth.entity.AuthRole;
import com.mocara.backend.auth.entity.RefreshTokenEntity;
import com.mocara.backend.auth.repo.AppUserRepository;
import com.mocara.backend.auth.repo.RefreshTokenRepository;
import com.mocara.backend.auth.security.AuthenticatedUser;
import com.mocara.backend.auth.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthRateLimitService authRateLimitService;

    public AuthService(
            AppUserRepository appUserRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthRateLimitService authRateLimitService
    ) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authRateLimitService = authRateLimitService;
    }

    @Transactional
    public AuthTokenResponseDto register(String email, String password, AuthRequestContext ctx) {
        authRateLimitService.checkOrThrow("register", ctx.ipAddress());
        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        AppUserEntity user = new AppUserEntity();
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setCreatedAtMs(System.currentTimeMillis());
        user.setRoles(Set.of(AuthRole.USER));
        AppUserEntity saved = appUserRepository.save(user);
        return issueTokens(saved, ctx);
    }

    @Transactional
    public AuthTokenResponseDto login(String email, String password, AuthRequestContext ctx) {
        authRateLimitService.checkOrThrow("login", ctx.ipAddress() + ":" + email.trim().toLowerCase());
        AppUserEntity user = appUserRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AuthException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }
        return issueTokens(user, ctx);
    }

    @Transactional
    public AuthTokenResponseDto refresh(String refreshToken, AuthRequestContext ctx) {
        Claims claims = jwtService.parseClaims(refreshToken);
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new AuthException("Invalid refresh token");
        }
        String hash = sha256(refreshToken);
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new AuthException("Invalid refresh token"));
        if (tokenEntity.isRevoked()) {
            // Reuse attack signal: previously rotated/revoked token is being presented again.
            refreshTokenRepository.revokeAllActiveByUserId(tokenEntity.getUser().getId());
            throw new AuthException("Invalid refresh token");
        }
        long nowMs = System.currentTimeMillis();
        boolean isExpired = tokenEntity.getExpiresAtMs() < nowMs;
        if (isExpired) {
            tokenEntity.setRevoked(true);
            refreshTokenRepository.save(tokenEntity);
            throw new AuthException("Invalid refresh token");
        }
        AppUserEntity user = tokenEntity.getUser();
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);
        return issueTokens(user, ctx);
    }

    @Transactional
    public void logout(String refreshToken) {
        String hash = sha256(refreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthTokenResponseDto issueTokens(AppUserEntity user, AuthRequestContext ctx) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(), user.getRoles());
        JwtService.TokenPayload access = jwtService.generateAccessToken(principal);
        JwtService.TokenPayload refresh = jwtService.generateRefreshToken(principal);

        String newRefreshHash = sha256(refresh.token());
        RefreshTokenEntity refreshEntity = new RefreshTokenEntity();
        refreshEntity.setUser(user);
        refreshEntity.setTokenHash(newRefreshHash);
        refreshEntity.setExpiresAtMs(refresh.expiresAtMs());
        refreshEntity.setCreatedAtMs(System.currentTimeMillis());
        refreshEntity.setDeviceId(ctx.deviceId());
        refreshEntity.setIpAddress(ctx.ipAddress());
        refreshEntity.setUserAgent(ctx.userAgent());
        refreshEntity.setRevoked(false);
        refreshTokenRepository.save(refreshEntity);

        return new AuthTokenResponseDto(
                "Bearer",
                access.token(),
                access.expiresAtMs(),
                refresh.token(),
                refresh.expiresAtMs(),
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
