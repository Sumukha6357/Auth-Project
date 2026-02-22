package com.example.idp.web.dto.admin;

import java.time.Instant;

public record KeyResponse(
    String kid,
    String algorithm,
    String status,
    Instant createdAt,
    Instant rotatedAt
) {}
