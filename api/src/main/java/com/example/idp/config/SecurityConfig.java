package com.example.idp.config;

import com.example.idp.security.CorrelationIdFilter;
import com.example.idp.security.EndpointRateLimitingFilter;
import com.example.idp.security.IdpUserPrincipal;
import com.example.idp.security.JwtAudienceValidator;
import com.example.idp.security.OAuthEndpointMetricsFilter;
import com.example.idp.security.oauth.DatabaseRegisteredClientRepository;
import com.example.idp.security.oauth.RefreshReuseDetectionFilter;
import com.example.idp.security.oauth.TrackingAuthorizationService;
import com.example.idp.service.IdpUserDetailsService;
import com.example.idp.service.KeyManagementService;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(IdpProperties.class)
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        try {
            return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        } catch (Throwable ex) {
            return new BCryptPasswordEncoder(12);
        }
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(org.springframework.core.env.Environment environment) {
        return AuthorizationServerSettings.builder()
            .issuer(environment.getProperty("spring.security.oauth2.authorizationserver.issuer", "http://localhost:9000"))
            .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public OAuth2AuthorizationService jdbcAuthorizationService(JdbcTemplate jdbcTemplate,
                                                               RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    @Primary
    public OAuth2AuthorizationService authorizationService(OAuth2AuthorizationService jdbcAuthorizationService,
                                                           com.example.idp.service.RefreshTokenSessionService refreshTokenSessionService,
                                                           com.example.idp.audit.AuditService auditService) {
        return new TrackingAuthorizationService(jdbcAuthorizationService, refreshTokenSessionService, auditService);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                         RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(DatabaseRegisteredClientRepository repository) {
        return repository;
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyManagementService keyManagementService) {
        return new NimbusJwtEncoder((jwkSelector, securityContext) -> jwkSelector.select(new com.nimbusds.jose.jwk.JWKSet(keyManagementService.activePrivateKey())));
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyManagementService keyManagementService,
                                 AuthorizationServerSettings settings,
                                 IdpProperties properties) {
        JwtDecoder decoder = keyManagementService.jwtDecoder(settings);
        if (decoder instanceof org.springframework.security.oauth2.jwt.NimbusJwtDecoder nimbus) {
            var issuerValidator = JwtValidators.createDefaultWithIssuer(settings.getIssuer());
            var audienceValidator = new JwtAudienceValidator(properties.getSecurity().getRequiredAudience());
            nimbus.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
        }
        return decoder;
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(IdpProperties properties) {
        return context -> {
            Authentication principal = context.getPrincipal();
            if (principal == null || !(principal.getPrincipal() instanceof IdpUserPrincipal user)) {
                return;
            }
            context.getClaims().subject(user.getUserId().toString());
            context.getClaims().claim("email", user.getUsername());
            context.getClaims().claim("email_verified", user.isEmailVerified());
            context.getClaims().claim("roles", user.getRoles());
                context.getClaims().claim("permissions", user.getPermissions());
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().audience(java.util.List.of(context.getRegisteredClient().getClientId(), properties.getSecurity().getRequiredAudience()));
            }
        };
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerFilterChain(HttpSecurity http,
                                                               CorrelationIdFilter correlationIdFilter,
                                                               RefreshReuseDetectionFilter refreshReuseDetectionFilter,
                                                               EndpointRateLimitingFilter endpointRateLimitingFilter,
                                                               OAuthEndpointMetricsFilter oauthEndpointMetricsFilter,
                                                               IdpProperties properties) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .oidc(Customizer.withDefaults());

        http.addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(refreshReuseDetectionFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(endpointRateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(oauthEndpointMetricsFilter, RefreshReuseDetectionFilter.class);
        if (properties.getSecurity().isHttpsOnly()) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http.exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
            new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"),
            new org.springframework.security.web.util.matcher.MediaTypeRequestMatcher(org.springframework.http.MediaType.TEXT_HTML)));
        http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http,
                                                       CorrelationIdFilter correlationIdFilter,
                                                       EndpointRateLimitingFilter endpointRateLimitingFilter,
                                                       IdpProperties properties,
                                                       IdpUserDetailsService userDetailsService) throws Exception {
        http.addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(endpointRateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        if (properties.getSecurity().isHttpsOnly()) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        if (properties.getCors().isEnabled()) {
            http.cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(properties.getCors().getAllowedOrigins());
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PATCH", "DELETE"));
                config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "X-Device-Id", "X-Correlation-Id"));
                return config;
            }));
        } else {
            http.cors(cors -> cors.disable());
        }

        http.csrf(csrf -> csrf.ignoringRequestMatchers(
            new AntPathRequestMatcher("/oauth2/**"),
            new AntPathRequestMatcher("/.well-known/**"),
            new AntPathRequestMatcher("/api/v1/public/**")));
        http.headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
            .frameOptions(frame -> frame.deny())
            .xssProtection(Customizer.withDefaults())
            .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
            .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
        );

        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/.well-known/**", "/api/v1/public/**").permitAll()
            .requestMatchers("/api/v1/admin/**").hasAuthority("admin:full_access")
            .requestMatchers("/api/v1/sessions/**").authenticated()
            .anyRequest().authenticated());

        http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
        http.formLogin(Customizer.withDefaults());
        http.userDetailsService(userDetailsService);
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }
}
