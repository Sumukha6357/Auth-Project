package com.example.idp.security;

import com.example.idp.audit.AuditService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class OAuthEndpointMetricsFilter extends OncePerRequestFilter {
    private final MeterRegistry meterRegistry;
    private final AuditService auditService;

    public OAuthEndpointMetricsFilter(MeterRegistry meterRegistry, AuditService auditService) {
        this.meterRegistry = meterRegistry;
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String path = request.getServletPath();
        boolean oauthPath = "/oauth2/token".equals(path) || "/oauth2/revoke".equals(path) || "/oauth2/introspect".equals(path);
        if (!oauthPath) {
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);

        String endpoint = path.replace("/oauth2/", "");
        String grantType = request.getParameter("grant_type");
        boolean success = response.getStatus() < 400;

        meterRegistry.counter(
            "idp.oauth.requests",
            "endpoint", endpoint,
            "grant_type", grantType == null ? "n/a" : grantType,
            "success", String.valueOf(success)
        ).increment();

        if ("/oauth2/revoke".equals(path)) {
            Map<String, Object> details = new HashMap<>();
            details.put("token_type_hint", request.getParameter("token_type_hint"));
            details.put("client_id", request.getParameter("client_id"));
            auditService.log(request, SecurityContextHolder.getContext().getAuthentication(), "TOKEN_REVOKE", "token", null, success, details);
        }
    }
}
