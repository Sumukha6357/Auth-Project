package com.example.idp.web.dto.admin;

import java.time.Instant;

public record AuditLogResponse(
    String id,
    String actorUserId,
    String eventType,
    String entityType,
    String entityId,
    boolean success,
    String ip,
    String userAgent,
    String correlationId,
    String details,
    Instant createdAt
) {}
