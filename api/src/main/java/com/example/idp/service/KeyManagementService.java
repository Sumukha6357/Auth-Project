package com.example.idp.service;

import com.example.idp.config.IdpProperties;
import com.example.idp.domain.JwkKeyEntity;
import com.example.idp.domain.JwkStatus;
import com.example.idp.repo.JwkKeyRepository;
import com.example.idp.util.AesGcmCrypto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeyManagementService {
    private final JwkKeyRepository jwkKeyRepository;
    private final AesGcmCrypto aesGcmCrypto;
    private final ObjectMapper objectMapper;
    private final IdpProperties idpProperties;

    public KeyManagementService(JwkKeyRepository jwkKeyRepository,
                                AesGcmCrypto aesGcmCrypto,
                                ObjectMapper objectMapper,
                                IdpProperties idpProperties) {
        this.jwkKeyRepository = jwkKeyRepository;
        this.aesGcmCrypto = aesGcmCrypto;
        this.objectMapper = objectMapper;
        this.idpProperties = idpProperties;
    }

    @PostConstruct
    @Transactional
    public void ensureInitialKey() {
        if (jwkKeyRepository.findFirstByStatusOrderByCreatedAtDesc(JwkStatus.ACTIVE).isEmpty()) {
            rotateKeys();
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void rotateKeysIfDue() {
        JwkKeyEntity active = jwkKeyRepository.findFirstByStatusOrderByCreatedAtDesc(JwkStatus.ACTIVE).orElse(null);
        if (active == null) {
            rotateKeys();
            return;
        }
        if (active.getCreatedAt().isBefore(Instant.now().minus(idpProperties.getKey().getRotationDays(), ChronoUnit.DAYS))) {
            rotateKeys();
        }
    }

    @Transactional
    public JwkKeyEntity rotateKeys() {
        jwkKeyRepository.findFirstByStatusOrderByCreatedAtDesc(JwkStatus.ACTIVE).ifPresent(existing -> {
            existing.setStatus(JwkStatus.RETIRED);
            existing.setRotatedAt(Instant.now());
            jwkKeyRepository.save(existing);
        });

        RSAKey rsaKey = generateRsa();
        JwkKeyEntity entity = new JwkKeyEntity();
        entity.setKid(rsaKey.getKeyID());
        entity.setAlgorithm(SignatureAlgorithm.RS256.getName());
        entity.setPublicJwk(toJson(rsaKey.toPublicJWK().toJSONObject()));
        entity.setPrivateJwkEncrypted(aesGcmCrypto.encrypt(rsaKey.toJSONString().getBytes(StandardCharsets.UTF_8)));
        entity.setStatus(JwkStatus.ACTIVE);
        entity.setCreatedAt(Instant.now());
        return jwkKeyRepository.save(entity);
    }

    public com.nimbusds.jose.jwk.source.JWKSource<SecurityContext> jwkSource() {
        return (selector, context) -> selector.select(new JWKSet(loadPublicJwks()));
    }

    public JwtDecoder jwtDecoder(AuthorizationServerSettings settings) {
        try {
            return NimbusJwtDecoder.withPublicKey(activePrivateKey().toRSAPublicKey()).build();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to build JWT decoder", e);
        }
    }

    public RSAKey activePrivateKey() {
        JwkKeyEntity active = jwkKeyRepository.findFirstByStatusOrderByCreatedAtDesc(JwkStatus.ACTIVE)
            .orElseThrow(() -> new IllegalStateException("No active key"));
        return parsePrivate(active);
    }

    public List<JWK> loadPublicJwks() {
        return jwkKeyRepository.findByStatusIn(List.of(JwkStatus.ACTIVE, JwkStatus.RETIRED)).stream()
            .map(this::parsePublic)
            .map(r -> (JWK) r)
            .toList();
    }

    public List<JwkKeyEntity> listKeys() {
        return jwkKeyRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    private RSAKey parsePublic(JwkKeyEntity entity) {
        try {
            Map<String, Object> map = objectMapper.readValue(entity.getPublicJwk(), objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            return RSAKey.parse(map);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse public JWK", e);
        }
    }

    private RSAKey parsePrivate(JwkKeyEntity entity) {
        try {
            String privateJwk = new String(aesGcmCrypto.decrypt(entity.getPrivateJwkEncrypted()), StandardCharsets.UTF_8);
            return RSAKey.parse(privateJwk);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse private JWK", e);
        }
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private RSAKey generateRsa() {
        try {
            return new RSAKeyGenerator(2048)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(UUID.randomUUID().toString())
                .generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Key generation failed", e);
        }
    }
}
