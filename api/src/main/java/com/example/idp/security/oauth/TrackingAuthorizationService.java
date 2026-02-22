package com.example.idp.security.oauth;

import com.example.idp.audit.AuditService;
import com.example.idp.service.RefreshTokenSessionService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthenticationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class TrackingAuthorizationService implements OAuth2AuthorizationService {
    private final OAuth2AuthorizationService delegate;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final AuditService auditService;

    public TrackingAuthorizationService(OAuth2AuthorizationService delegate,
                                        RefreshTokenSessionService refreshTokenSessionService,
                                        AuditService auditService) {
        this.delegate = delegate;
        this.refreshTokenSessionService = refreshTokenSessionService;
        this.auditService = auditService;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        delegate.save(authorization);
        OAuth2RefreshToken refreshToken = authorization.getRefreshToken() != null ? authorization.getRefreshToken().getToken() : null;
        if (refreshToken == null) {
            return;
        }
        UUID userId = parseUserId(authorization.getPrincipalName());
        String deviceId = "default";
        HttpServletRequest request = currentRequest();
        if (request != null && request.getHeader("X-Device-Id") != null) {
            deviceId = request.getHeader("X-Device-Id");
        }
        refreshTokenSessionService.storeIssuedToken(
            refreshToken.getTokenValue(),
            RefreshTokenContextHolder.getParentHash(),
            userId,
            authorization.getRegisteredClientId(),
            deviceId,
            refreshToken.getExpiresAt() != null ? refreshToken.getExpiresAt() : Instant.now().plusSeconds(2592000),
            request != null ? request.getRemoteAddr() : null,
            request != null ? request.getHeader("User-Agent") : null
        );
        auditService.log(request, null, "TOKEN_ISSUED", "authorization", authorization.getId(), true,
            java.util.Map.of("client_id", authorization.getRegisteredClientId(), "grant_type", authorization.getAuthorizationGrantType().getValue()));
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        delegate.remove(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return delegate.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, org.springframework.security.oauth2.server.authorization.OAuth2TokenType tokenType) {
        return delegate.findByToken(token, tokenType);
    }

    private UUID parseUserId(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (Exception ignored) {
            return null;
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
