package com.example.idp.web;

import com.example.idp.audit.AuditService;
import com.example.idp.security.CurrentUser;
import com.example.idp.service.RefreshTokenSessionService;
import com.example.idp.web.dto.RevokeSessionRequest;
import com.example.idp.web.dto.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions/me")
public class SessionController {
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final CurrentUser currentUser;
    private final AuditService auditService;

    public SessionController(RefreshTokenSessionService refreshTokenSessionService,
                             CurrentUser currentUser,
                             AuditService auditService) {
        this.refreshTokenSessionService = refreshTokenSessionService;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    @GetMapping
    public List<SessionResponse> list(Authentication authentication) {
        UUID userId = currentUser.requireUserId(authentication);
        return refreshTokenSessionService.activeSessions(userId).stream()
            .map(s -> new SessionResponse(
                s.getId().toString(),
                s.getClientId(),
                s.getDeviceId(),
                s.getIssuedAt(),
                s.getExpiresAt(),
                s.getIp(),
                s.getUserAgent()))
            .toList();
    }

    @PostMapping("/revoke")
    public void revoke(@Valid @RequestBody RevokeSessionRequest request,
                       Authentication authentication,
                       HttpServletRequest httpRequest) {
        UUID userId = currentUser.requireUserId(authentication);
        refreshTokenSessionService.revokeByDevice(userId, request.deviceId());
        auditService.log(httpRequest, authentication, "SESSION_REVOKE_DEVICE", "user", userId.toString(), true,
            Map.of("device_id", request.deviceId()));
    }

    @PostMapping("/revoke-all")
    public void revokeAll(Authentication authentication, HttpServletRequest httpRequest) {
        UUID userId = currentUser.requireUserId(authentication);
        refreshTokenSessionService.revokeAll(userId);
        auditService.log(httpRequest, authentication, "SESSION_REVOKE_ALL", "user", userId.toString(), true, Map.of());
    }
}
