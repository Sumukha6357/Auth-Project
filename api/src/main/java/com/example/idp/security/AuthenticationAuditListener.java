package com.example.idp.security;

import com.example.idp.audit.AuditService;
import com.example.idp.service.IdpUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuthenticationAuditListener {
    private final IdpUserDetailsService userDetailsService;
    private final AuditService auditService;

    public AuthenticationAuditListener(IdpUserDetailsService userDetailsService, AuditService auditService) {
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = String.valueOf(event.getAuthentication().getPrincipal());
        userDetailsService.onAuthenticationFailure(username);
        auditService.log(currentRequest(), null, "LOGIN_FAILURE", "user", username, false, Map.of("reason", "bad_credentials"));
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        if (auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            userDetailsService.onAuthenticationSuccess(auth.getName());
            auditService.log(currentRequest(), auth, "LOGIN_SUCCESS", "user", auth.getName(), true, Map.of());
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
