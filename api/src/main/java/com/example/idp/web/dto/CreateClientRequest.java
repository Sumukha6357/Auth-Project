package com.example.idp.web.dto;

import com.example.idp.domain.ClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record CreateClientRequest(
    @NotBlank String clientId,
    String clientSecret,
    @NotBlank String name,
    @NotNull ClientType type,
    Set<String> redirectUris,
    Set<String> postLogoutRedirectUris,
    Set<String> grantTypes,
    Set<String> scopes,
    boolean requirePkce
) {}
