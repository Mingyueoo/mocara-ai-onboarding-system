package com.mocara.backend.auth.security;

import com.mocara.backend.auth.entity.AuthRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return user;
    }

    public boolean isAdmin() {
        return currentUser().roles().contains(AuthRole.ADMIN);
    }
}
