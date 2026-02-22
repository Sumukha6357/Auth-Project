package com.example.idp.web.dto;

import java.time.Instant;

public record SessionResponse(
    String id,
    String clientId,
    String deviceId,
    Instant issuedAt,
    Instant expiresAt,
    String ip,
    String userAgent
) {}
