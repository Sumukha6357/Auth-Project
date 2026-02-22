package com.example.idp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.idp.domain.RefreshTokenSessionEntity;
import com.example.idp.repo.RefreshTokenSessionRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "RUN_TESTCONTAINERS", matches = "true")
class IdpIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("idp")
        .withUsername("idp")
        .withPassword("idp");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("POSTGRES_URL", postgres::getJdbcUrl);
        registry.add("POSTGRES_USER", postgres::getUsername);
        registry.add("POSTGRES_PASSWORD", postgres::getPassword);
        registry.add("IDP_KEY_ENCRYPTION_SECRET", () -> "test-secret-key");
        registry.add("IDP_ADMIN_BOOTSTRAP_EMAIL", () -> "admin@example.com");
        registry.add("IDP_ADMIN_BOOTSTRAP_PASSWORD", () -> "Admin123!!");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RefreshTokenSessionRepository refreshTokenSessionRepository;

    @BeforeEach
    void clearSessions() {
        refreshTokenSessionRepository.deleteAll();
    }

    @Test
    void openIdConfigurationAvailable() throws Exception {
        mockMvc.perform(get("/.well-known/openid-configuration"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.issuer").exists())
            .andExpect(jsonPath("$.authorization_endpoint").exists())
            .andExpect(jsonPath("$.token_endpoint").exists());
    }

    @Test
    void clientCredentialsTokenWorks() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("system-service", "password"))
                .contentType("application/x-www-form-urlencoded")
                .param("grant_type", "client_credentials")
                .param("scope", "idp.read"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").exists())
            .andExpect(jsonPath("$.token_type").value("Bearer"));
    }

    @Test
    void authorizationCodeWithPkceRedirectsWithCode() throws Exception {
        mockMvc.perform(get("/oauth2/authorize")
                .with(user("admin@example.com").password("x").authorities(() -> "admin:full_access"))
                .param("response_type", "code")
                .param("client_id", "web-portal")
                .param("redirect_uri", "http://localhost:3000/oidc/callback")
                .param("scope", "openid profile")
                .param("state", "abc123")
                .param("code_challenge", "nKP31Hf0M1fOFY0R8v33b5hKfEOQ-Z6fMUt0wVj0Lxw")
                .param("code_challenge_method", "S256"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("code=")))
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("state=abc123")));
    }

    @Test
    void jwksEndpointReturnsKeys() throws Exception {
        mockMvc.perform(get("/oauth2/jwks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.keys").isArray());
    }

    @Test
    void revokeAllInvalidatesRefreshSessions() throws Exception {
        UUID userId = UUID.randomUUID();
        RefreshTokenSessionEntity entity = new RefreshTokenSessionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setClientId("web-portal");
        entity.setDeviceId("device-1");
        entity.setRefreshTokenHash("hash");
        entity.setIssuedAt(Instant.now());
        entity.setExpiresAt(Instant.now().plusSeconds(3600));
        refreshTokenSessionRepository.save(entity);

        mockMvc.perform(post("/api/v1/sessions/me/revoke-all")
                .with(jwt().jwt(jwt -> jwt.subject(userId.toString()).claim("permissions", java.util.List.of("sessions:manage")))))
            .andExpect(status().isOk());

        RefreshTokenSessionEntity updated = refreshTokenSessionRepository.findById(entity.getId()).orElseThrow();
        assertThat(updated.getRevokedAt()).isNotNull();
    }
}
