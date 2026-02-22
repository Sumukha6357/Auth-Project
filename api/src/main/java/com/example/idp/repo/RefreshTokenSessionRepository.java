package com.example.idp.repo;

import com.example.idp.domain.RefreshTokenSessionEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSessionEntity, UUID>, JpaSpecificationExecutor<RefreshTokenSessionEntity> {
    Optional<RefreshTokenSessionEntity> findByRefreshTokenHash(String refreshTokenHash);

    List<RefreshTokenSessionEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

    List<RefreshTokenSessionEntity> findByUserIdAndClientIdAndDeviceIdAndRevokedAtIsNull(UUID userId, String clientId, String deviceId);

    List<RefreshTokenSessionEntity> findByUserIdAndClientIdAndRevokedAtIsNull(UUID userId, String clientId);

    List<RefreshTokenSessionEntity> findByParentTokenHash(String parentTokenHash);

    long deleteByExpiresAtBefore(Instant instant);
}
