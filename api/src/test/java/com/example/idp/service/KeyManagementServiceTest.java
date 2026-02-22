package com.example.idp.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.idp.config.IdpProperties;
import com.example.idp.domain.JwkKeyEntity;
import com.example.idp.domain.JwkStatus;
import com.example.idp.repo.JwkKeyRepository;
import com.example.idp.util.AesGcmCrypto;
import com.example.idp.util.SecretResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.env.MockEnvironment;

class KeyManagementServiceTest {
    @Test
    void rotatesActiveToRetired() {
        JwkKeyRepository repo = Mockito.mock(JwkKeyRepository.class);
        IdpProperties props = new IdpProperties();
        props.getKey().setEncryptionSecret("test-secret-encryption-key");
        AesGcmCrypto crypto = new AesGcmCrypto(props, new SecretResolver(new MockEnvironment()));
        crypto.init();

        JwkKeyEntity active = new JwkKeyEntity();
        active.setKid("old");
        active.setStatus(JwkStatus.ACTIVE);
        active.setCreatedAt(Instant.now().minusSeconds(1000));

        Mockito.when(repo.findFirstByStatusOrderByCreatedAtDesc(JwkStatus.ACTIVE)).thenReturn(Optional.of(active));
        Mockito.when(repo.save(Mockito.any(JwkKeyEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        KeyManagementService service = new KeyManagementService(repo, crypto, new ObjectMapper(), props);
        JwkKeyEntity newKey = service.rotateKeys();

        assertThat(active.getStatus()).isEqualTo(JwkStatus.RETIRED);
        assertThat(newKey.getStatus()).isEqualTo(JwkStatus.ACTIVE);
    }
}
