package com.example.idp.service;

import com.example.idp.domain.ClientType;
import com.example.idp.domain.OAuthClientEntity;
import com.example.idp.repo.OAuthClientRepository;
import com.example.idp.web.ApiException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientAdminService {
    private final OAuthClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientAdminService(OAuthClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ClientResult createClient(String clientId,
                                     String clientSecret,
                                     String name,
                                     ClientType type,
                                     Set<String> redirectUris,
                                     Set<String> postLogoutRedirectUris,
                                     Set<String> grantTypes,
                                     Set<String> scopes,
                                     boolean requirePkce) {
        if (clientRepository.existsById(clientId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Client already exists");
        }
        if (type == ClientType.PUBLIC && !requirePkce) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Public clients must require PKCE");
        }
        validateRedirectUris(redirectUris);
        validateRedirectUris(postLogoutRedirectUris);
        String issuedSecret = clientSecret;
        if (type == ClientType.CONFIDENTIAL && (issuedSecret == null || issuedSecret.isBlank())) {
            issuedSecret = com.example.idp.util.TokenUtils.randomUrlSafeToken(24);
        }
        OAuthClientEntity entity = new OAuthClientEntity();
        entity.setClientId(clientId);
        entity.setClientSecretHash(issuedSecret == null ? null : passwordEncoder.encode(issuedSecret));
        entity.setName(name);
        entity.setType(type);
        entity.setRedirectUris(redirectUris.toArray(new String[0]));
        entity.setPostLogoutRedirectUris(postLogoutRedirectUris.toArray(new String[0]));
        entity.setGrantTypes(grantTypes.toArray(new String[0]));
        entity.setScopes(scopes.toArray(new String[0]));
        entity.setRequirePkce(requirePkce);
        entity.setEnabled(true);
        entity.setCreatedAt(Instant.now());
        OAuthClientEntity saved = clientRepository.save(entity);
        return new ClientResult(saved, issuedSecret);
    }

    @Transactional
    public ClientResult patchClient(String clientId, Boolean enabled, boolean rotateSecret) {
        OAuthClientEntity client = clientRepository.findById(clientId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Client not found"));

        String rotatedSecret = null;
        if (enabled != null) {
            client.setEnabled(enabled);
        }
        if (rotateSecret && client.getType() == ClientType.CONFIDENTIAL) {
            rotatedSecret = com.example.idp.util.TokenUtils.randomUrlSafeToken(24);
            client.setClientSecretHash(passwordEncoder.encode(rotatedSecret));
        }
        OAuthClientEntity saved = clientRepository.save(client);
        return new ClientResult(saved, rotatedSecret);
    }

    @Transactional
    public void ensureDefaultServiceSecret(String rawSecret) {
        clientRepository.findById("demo-service").ifPresent(client -> {
            if (client.getClientSecretHash() == null || client.getClientSecretHash().isBlank()) {
                client.setClientSecretHash(passwordEncoder.encode(rawSecret));
                clientRepository.save(client);
            }
        });
    }

    @Transactional(readOnly = true)
    public List<OAuthClientEntity> listClients() {
        return clientRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "clientId"));
    }

    private void validateRedirectUris(Set<String> redirectUris) {
        for (String uri : redirectUris) {
            if (uri.contains("*")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Redirect URIs cannot contain wildcards");
            }
            try {
                java.net.URI parsed = java.net.URI.create(uri);
                if (parsed.getScheme() == null || parsed.getHost() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Redirect URI must be absolute: " + uri);
                }
            } catch (IllegalArgumentException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid redirect URI: " + uri);
            }
        }
    }

    public record ClientResult(OAuthClientEntity client, String rawSecret) {}
}
