package com.example.idp.service;

import com.example.idp.audit.AuditService;
import com.example.idp.domain.RefreshTokenSessionEntity;
import com.example.idp.repo.RefreshTokenSessionRepository;
import com.example.idp.util.HashUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenSessionService {
    private final RefreshTokenSessionRepository repository;
    private final AuditService auditService;

    public RefreshTokenSessionService(RefreshTokenSessionRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional
    public void storeIssuedToken(String rawRefreshToken,
                                 String parentTokenHash,
                                 UUID userId,
                                 String clientId,
                                 String deviceId,
                                 Instant expiresAt,
                                 String ip,
                                 String userAgent) {
        if (rawRefreshToken == null || userId == null) {
            return;
        }
        String tokenHash = HashUtils.sha256(rawRefreshToken);
        RefreshTokenSessionEntity session = new RefreshTokenSessionEntity();
        session.setId(UUID.randomUUID());
        session.setUserId(userId);
        session.setClientId(clientId);
        session.setDeviceId(deviceId == null ? "default" : deviceId);
        session.setRefreshTokenHash(tokenHash);
        session.setParentTokenHash(parentTokenHash);
        session.setIssuedAt(Instant.now());
        session.setExpiresAt(expiresAt);
        session.setIp(ip);
        session.setUserAgent(userAgent);
        repository.save(session);

        if (parentTokenHash != null) {
            repository.findByRefreshTokenHash(parentTokenHash).ifPresent(parent -> {
                parent.setRevokedAt(Instant.now());
                repository.save(parent);
            });
        }
    }

    @Transactional(readOnly = true)
    public boolean isRevokedOrMissing(String rawToken) {
        String hash = HashUtils.sha256(rawToken);
        return repository.findByRefreshTokenHash(hash)
            .map(s -> s.getRevokedAt() != null || s.getExpiresAt().isBefore(Instant.now()))
            .orElse(true);
    }

    @Transactional
    public void onRefreshReuse(String rawToken, HttpServletRequest request, Authentication actor) {
        String hash = HashUtils.sha256(rawToken);
        repository.findByRefreshTokenHash(hash).ifPresent(session -> {
            revokeFamily(session.getRefreshTokenHash());
            auditService.log(request, actor, "REFRESH_TOKEN_REUSE", "session", session.getId().toString(), false,
                Map.of("client_id", session.getClientId(), "device_id", session.getDeviceId()));
        });
    }

    @Transactional
    public void revokeByDevice(UUID userId, String deviceId) {
        repository.findByUserIdAndRevokedAtIsNull(userId).stream()
            .filter(s -> s.getDeviceId().equals(deviceId))
            .forEach(s -> {
                s.setRevokedAt(Instant.now());
                repository.save(s);
            });
    }

    @Transactional
    public void revokeAll(UUID userId) {
        repository.findByUserIdAndRevokedAtIsNull(userId).forEach(s -> {
            s.setRevokedAt(Instant.now());
            repository.save(s);
        });
    }

    @Transactional(readOnly = true)
    public List<RefreshTokenSessionEntity> activeSessions(UUID userId) {
        return repository.findByUserIdAndRevokedAtIsNull(userId);
    }

    @Transactional
    public void revokeFamily(String tokenHash) {
        repository.findByRefreshTokenHash(tokenHash).ifPresent(current -> {
            current.setRevokedAt(Instant.now());
            repository.save(current);
            List<RefreshTokenSessionEntity> children = repository.findByParentTokenHash(current.getRefreshTokenHash());
            for (RefreshTokenSessionEntity child : children) {
                revokeFamily(child.getRefreshTokenHash());
            }
        });
    }

    @Transactional(readOnly = true)
    public Page<RefreshTokenSessionEntity> listSessions(UUID userId, String clientId, Pageable pageable) {
        return repository.findAll((root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.isNull(root.get("revokedAt")));
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (clientId != null && !clientId.isBlank()) {
                predicates.add(cb.equal(root.get("clientId"), clientId));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

    @Transactional
    public boolean revokeSessionById(UUID sessionId) {
        return repository.findById(sessionId).map(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(Instant.now());
                repository.save(session);
            }
            return true;
        }).orElse(false);
    }
}
