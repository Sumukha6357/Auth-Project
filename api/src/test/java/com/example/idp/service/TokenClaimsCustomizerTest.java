package com.example.idp.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.idp.config.IdpProperties;
import com.example.idp.config.SecurityConfig;
import com.example.idp.security.IdpUserPrincipal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

class TokenClaimsCustomizerTest {
    @Test
    void mapsRoleAndPermissionClaims() {
        SecurityConfig config = new SecurityConfig();
        var customizer = config.jwtTokenCustomizer(new IdpProperties());

        IdpUserPrincipal principal = new IdpUserPrincipal(
            UUID.randomUUID(), "user@example.com", "hash", true, true, true,
            Set.of("ADMIN"), Set.of("admin:full_access"));

        JwtEncodingContext context = Mockito.mock(JwtEncodingContext.class);
        JwtClaimsSet.Builder builder = Mockito.mock(JwtClaimsSet.Builder.class);
        RegisteredClient client = RegisteredClient.withId("id")
            .clientId("demo")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("idp.read")
            .build();
        when(context.getPrincipal()).thenReturn(new UsernamePasswordAuthenticationToken(principal, null));
        when(context.getClaims()).thenReturn(builder);
        when(context.getRegisteredClient()).thenReturn(client);
        when(context.getTokenType()).thenReturn(org.springframework.security.oauth2.server.authorization.OAuth2TokenType.ACCESS_TOKEN);
        when(builder.subject(any())).thenReturn(builder);
        when(builder.claim(any(), any())).thenReturn(builder);
        when(builder.audience(any())).thenReturn(builder);

        customizer.customize(context);

        verify(builder).claim(eq("roles"), eq(Set.of("ADMIN")));
        verify(builder).claim(eq("permissions"), eq(Set.of("admin:full_access")));
    }
}
