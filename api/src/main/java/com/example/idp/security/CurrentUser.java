package com.example.idp.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public UUID requireUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return UUID.fromString(jwt.getSubject());
        }
        if (authentication != null && authentication.getPrincipal() instanceof IdpUserPrincipal principal) {
            return principal.getUserId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}
