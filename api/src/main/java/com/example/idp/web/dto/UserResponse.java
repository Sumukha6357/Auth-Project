package com.example.idp.web.dto;

import java.util.Set;

public record UserResponse(
    String id,
    String email,
    boolean emailVerified,
    String status,
    Set<String> roles,
    String verificationToken
) {}
