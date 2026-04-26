package com.mocara.backend.api.v1.controller;

import com.mocara.backend.auth.dto.AuthTokenResponseDto;
import com.mocara.backend.auth.dto.LoginRequestDto;
import com.mocara.backend.auth.dto.LogoutRequestDto;
import com.mocara.backend.auth.dto.RefreshTokenRequestDto;
import com.mocara.backend.auth.dto.RegisterRequestDto;
import com.mocara.backend.auth.service.AuthRequestContext;
import com.mocara.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthTokenResponseDto register(@Valid @RequestBody RegisterRequestDto request, HttpServletRequest httpRequest) {
        return authService.register(request.email(), request.password(), requestContext(httpRequest));
    }

    @PostMapping("/login")
    public AuthTokenResponseDto login(@Valid @RequestBody LoginRequestDto request, HttpServletRequest httpRequest) {
        return authService.login(request.email(), request.password(), requestContext(httpRequest));
    }

    @PostMapping("/refresh")
    public AuthTokenResponseDto refresh(@Valid @RequestBody RefreshTokenRequestDto request, HttpServletRequest httpRequest) {
        return authService.refresh(request.refreshToken(), requestContext(httpRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDto request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private AuthRequestContext requestContext(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ipAddress = (forwardedFor != null && !forwardedFor.isBlank())
                ? forwardedFor.split(",")[0].trim()
                : request.getRemoteAddr();
        String deviceId = request.getHeader("X-Device-Id");
        String userAgent = request.getHeader("User-Agent");
        return new AuthRequestContext(deviceId, ipAddress, userAgent);
    }
}
