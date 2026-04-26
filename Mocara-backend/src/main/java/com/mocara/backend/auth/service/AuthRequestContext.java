package com.mocara.backend.auth.service;

public record AuthRequestContext(
        String deviceId,
        String ipAddress,
        String userAgent
) {}
