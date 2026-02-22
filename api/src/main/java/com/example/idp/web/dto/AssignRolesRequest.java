package com.example.idp.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record AssignRolesRequest(
    Set<@NotBlank String> roles
) {}
