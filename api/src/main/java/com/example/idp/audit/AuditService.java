package com.example.idp.audit;

import com.example.idp.domain.AuditLogEntity;
import com.example.idp.repo.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    public void log(HttpServletRequest request,
                    Authentication authentication,
                    String eventType,
                    String entityType,
                    String entityId,
                    boolean success,
                    Map<String, Object> details) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setActorUserId(extractUserId(authentication));
        entity.setEventType(eventType);
        entity.setEntityType(entityType);
        entity.setEntityId(entityId);
        entity.setSuccess(success);
        entity.setIp(request != null ? request.getRemoteAddr() : null);
        entity.setUserAgent(request != null ? request.getHeader("User-Agent") : null);
        entity.setCorrelationId(MDC.get("correlationId"));
        entity.setDetails(toJson(details));
        entity.setCreatedAt(Instant.now());
        auditLogRepository.save(entity);
        meterRegistry.counter(
            "idp.audit.events",
            "event_type", eventType,
            "entity_type", entityType,
            "success", String.valueOf(success)
        ).increment();
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            try {
                return UUID.fromString(jwt.getSubject());
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }

    private String toJson(Map<String, Object> details) {
        try {
            return objectMapper.writeValueAsString(details == null ? Map.of() : details);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
