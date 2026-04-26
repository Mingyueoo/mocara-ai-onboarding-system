package com.mocara.backend.auth.security;

import com.mocara.backend.auth.entity.AuthRole;

import java.util.Set;

public record AuthenticatedUser(Long userId, String email, Set<AuthRole> roles) {}
