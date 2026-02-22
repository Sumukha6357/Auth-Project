package com.example.idp.security.oauth;

import com.example.idp.service.RefreshTokenSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RefreshReuseDetectionFilter extends OncePerRequestFilter {
    private final RefreshTokenSessionService refreshTokenSessionService;

    public RefreshReuseDetectionFilter(RefreshTokenSessionService refreshTokenSessionService) {
        this.refreshTokenSessionService = refreshTokenSessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (!"/oauth2/token".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!"refresh_token".equals(request.getParameter("grant_type"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = request.getParameter("refresh_token");
        if (refreshToken == null || refreshToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (refreshTokenSessionService.isRevokedOrMissing(refreshToken)) {
            refreshTokenSessionService.onRefreshReuse(refreshToken, request, null);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"invalid_grant\",\"error_description\":\"refresh token reuse detected\"}");
            return;
        }

        RefreshTokenContextHolder.setParentHash(com.example.idp.util.HashUtils.sha256(refreshToken));
        try {
            filterChain.doFilter(request, response);
        } finally {
            RefreshTokenContextHolder.clear();
        }
    }
}
