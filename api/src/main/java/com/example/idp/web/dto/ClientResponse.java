package com.example.idp.web.dto;

import com.example.idp.domain.ClientType;
import java.util.Set;

public record ClientResponse(
    String clientId,
    String name,
    ClientType type,
    boolean enabled,
    boolean requirePkce,
    Set<String> grantTypes,
    Set<String> scopes,
    Set<String> redirectUris,
    String rawSecret
) {}
