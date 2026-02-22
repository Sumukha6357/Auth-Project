package com.example.idp.web.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminSessionRevokeRequest(
    @NotBlank String sessionId
) {}
