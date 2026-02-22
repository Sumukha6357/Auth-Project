package com.example.idp.bootstrap;

import com.example.idp.config.IdpProperties;
import com.example.idp.util.SecretResolver;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupValidationService {
    private final IdpProperties properties;
    private final SecretResolver secretResolver;
    private final Environment environment;

    public StartupValidationService(IdpProperties properties, SecretResolver secretResolver, Environment environment) {
        this.properties = properties;
        this.secretResolver = secretResolver;
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {
        if (!properties.getSecurity().isFailFastSecrets()) {
            return;
        }

        require("POSTGRES_URL", environment.getProperty("POSTGRES_URL"));
        require("POSTGRES_USER", environment.getProperty("POSTGRES_USER"));
        require("POSTGRES_PASSWORD", environment.getProperty("POSTGRES_PASSWORD"));
        require("IDP_ISSUER", environment.getProperty("spring.security.oauth2.authorizationserver.issuer"));
        require("IDP_KEY_ENCRYPTION_SECRET", secretResolver.resolve(properties.getKey().getEncryptionSecret(), "IDP_KEY_ENCRYPTION_SECRET"));
        require("IDP_ADMIN_BOOTSTRAP_EMAIL", secretResolver.resolve(properties.getBootstrap().getAdminEmail(), "IDP_ADMIN_BOOTSTRAP_EMAIL"));
        require("IDP_ADMIN_BOOTSTRAP_PASSWORD", secretResolver.resolve(properties.getBootstrap().getAdminPassword(), "IDP_ADMIN_BOOTSTRAP_PASSWORD"));
    }

    private void require(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration: " + key);
        }
    }
}
