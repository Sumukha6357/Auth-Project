package com.example.idp.security;

import com.example.idp.config.IdpProperties;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class EndpointRateLimitingFilter extends OncePerRequestFilter {
    private final IdpProperties properties;
    private final MeterRegistry meterRegistry;

    private final Map<String, CounterWindow> tokenWindows = new ConcurrentHashMap<>();
    private final Map<String, CounterWindow> loginWindows = new ConcurrentHashMap<>();
    private final Map<String, CounterWindow> introspectWindows = new ConcurrentHashMap<>();

    public EndpointRateLimitingFilter(IdpProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String ip = request.getRemoteAddr();
        boolean blocked = false;

        if ("/oauth2/token".equals(servletPath)) {
            blocked = !allow(ip, tokenWindows, properties.getRateLimit().getTokenEndpointPerMinute());
            if (blocked) {
                meterRegistry.counter("idp.ratelimit.blocked", "endpoint", "oauth2_token").increment();
            }
        } else if ("/login".equals(servletPath)) {
            blocked = !allow(ip, loginWindows, properties.getRateLimit().getLoginEndpointPerMinute());
            if (blocked) {
                meterRegistry.counter("idp.ratelimit.blocked", "endpoint", "login").increment();
            }
        } else if ("/oauth2/introspect".equals(servletPath)) {
            blocked = !allow(ip, introspectWindows, properties.getRateLimit().getIntrospectEndpointPerMinute());
            if (blocked) {
                meterRegistry.counter("idp.ratelimit.blocked", "endpoint", "oauth2_introspect").increment();
            }
        }

        if (blocked) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"rate_limited\",\"error_description\":\"Too many requests\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean allow(String key, Map<String, CounterWindow> windows, int limitPerMinute) {
        CounterWindow window = windows.computeIfAbsent(key, ignored -> new CounterWindow(Instant.now().getEpochSecond() / 60, 0));
        synchronized (window) {
            long currentWindow = Instant.now().getEpochSecond() / 60;
            if (window.windowId != currentWindow) {
                window.windowId = currentWindow;
                window.count = 0;
            }
            window.count++;
            return window.count <= limitPerMinute;
        }
    }

    private static final class CounterWindow {
        private long windowId;
        private int count;

        private CounterWindow(long windowId, int count) {
            this.windowId = windowId;
            this.count = count;
        }
    }
}
