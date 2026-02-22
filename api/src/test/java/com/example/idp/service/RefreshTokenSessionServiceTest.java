package com.example.idp.service;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.idp.audit.AuditService;
import com.example.idp.domain.RefreshTokenSessionEntity;
import com.example.idp.repo.RefreshTokenSessionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RefreshTokenSessionServiceTest {
    @Test
    void reuseDetectionRevokesFamily() {
        RefreshTokenSessionRepository repo = Mockito.mock(RefreshTokenSessionRepository.class);
        AuditService auditService = Mockito.mock(AuditService.class);
        RefreshTokenSessionService service = new RefreshTokenSessionService(repo, auditService);

        RefreshTokenSessionEntity parent = new RefreshTokenSessionEntity();
        parent.setId(UUID.randomUUID());
        parent.setRefreshTokenHash("parent");
        parent.setExpiresAt(Instant.now().plusSeconds(1000));

        RefreshTokenSessionEntity child = new RefreshTokenSessionEntity();
        child.setId(UUID.randomUUID());
        child.setRefreshTokenHash("child");
        child.setParentTokenHash("parent");
        child.setExpiresAt(Instant.now().plusSeconds(1000));

        when(repo.findByRefreshTokenHash("parent")).thenReturn(Optional.of(parent));
        when(repo.findByParentTokenHash("parent")).thenReturn(List.of(child));
        when(repo.findByParentTokenHash("child")).thenReturn(List.of());
        when(repo.findByRefreshTokenHash("child")).thenReturn(Optional.of(child));

        service.revokeFamily("parent");

        verify(repo, atLeastOnce()).save(parent);
        verify(repo, atLeastOnce()).save(child);
    }
}
