package com.example.idp.web.dto.admin;

import java.util.List;

public record PagedResponse<T>(
    List<T> items,
    long total,
    int page,
    int size
) {}
