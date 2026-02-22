package com.example.idp.security.oauth;

import com.example.idp.domain.ClientType;
import com.example.idp.domain.OAuthClientEntity;
import com.example.idp.repo.OAuthClientRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

@Component
public class DatabaseRegisteredClientRepository implements RegisteredClientRepository {
    private final OAuthClientRepository clientRepository;

    public DatabaseRegisteredClientRepository(OAuthClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        OAuthClientEntity entity = clientRepository.findById(registeredClient.getClientId()).orElseGet(OAuthClientEntity::new);
        entity.setClientId(registeredClient.getClientId());
        entity.setClientSecretHash(registeredClient.getClientSecret());
        entity.setName(registeredClient.getClientName());
        entity.setType(registeredClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)
            ? ClientType.PUBLIC : ClientType.CONFIDENTIAL);
        entity.setGrantTypes(registeredClient.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).toArray(String[]::new));
        entity.setScopes(registeredClient.getScopes().toArray(new String[0]));
        entity.setRedirectUris(registeredClient.getRedirectUris().toArray(new String[0]));
        entity.setPostLogoutRedirectUris(registeredClient.getPostLogoutRedirectUris().toArray(new String[0]));
        entity.setRequirePkce(registeredClient.getClientSettings().isRequireProofKey());
        entity.setEnabled(true);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(java.time.Instant.now());
        }
        clientRepository.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository.findById(id).map(this::toRegisteredClient).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findById(clientId).filter(OAuthClientEntity::isEnabled).map(this::toRegisteredClient).orElse(null);
    }

    private RegisteredClient toRegisteredClient(OAuthClientEntity entity) {
        Set<AuthorizationGrantType> grants = Arrays.stream(entity.getGrantTypes())
            .map(AuthorizationGrantType::new)
            .collect(Collectors.toSet());

        RegisteredClient.Builder builder = RegisteredClient.withId(entity.getClientId())
            .clientId(entity.getClientId())
            .clientName(entity.getName())
            .scopes(s -> s.addAll(new HashSet<>(Arrays.asList(entity.getScopes()))))
            .redirectUris(u -> u.addAll(new HashSet<>(Arrays.asList(entity.getRedirectUris()))))
            .postLogoutRedirectUris(u -> u.addAll(new HashSet<>(Arrays.asList(entity.getPostLogoutRedirectUris()))))
            .authorizationGrantTypes(g -> g.addAll(grants))
            .clientSettings(ClientSettings.builder()
                .requireProofKey(entity.isRequirePkce())
                .requireAuthorizationConsent(false)
                .build())
            .tokenSettings(TokenSettings.builder().reuseRefreshTokens(false).build());

        if (entity.getType() == ClientType.PUBLIC) {
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
        } else {
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
            builder.clientSecret(entity.getClientSecretHash());
        }
        return builder.build();
    }
}
