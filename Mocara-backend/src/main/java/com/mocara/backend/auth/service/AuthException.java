package com.mocara.backend.auth.service;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
