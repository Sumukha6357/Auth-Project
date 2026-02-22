package com.example.idp.web.dto;

public record PatchClientRequest(
    Boolean enabled,
    boolean rotateSecret
) {}
