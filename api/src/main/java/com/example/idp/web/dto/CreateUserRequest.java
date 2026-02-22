package com.example.idp.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank String password,
    boolean emailVerified,
    Set<String> roles
) {}
