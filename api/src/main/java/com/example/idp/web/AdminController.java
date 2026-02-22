package com.example.idp.web;

import com.example.idp.audit.AuditService;
import com.example.idp.domain.OAuthClientEntity;
import com.example.idp.domain.UserEntity;
import com.example.idp.repo.AuditLogRepository;
import com.example.idp.repo.PermissionRepository;
import com.example.idp.repo.RoleRepository;
import com.example.idp.service.ClientAdminService;
import com.example.idp.service.KeyManagementService;
import com.example.idp.service.RefreshTokenSessionService;
import com.example.idp.service.UserAdminService;
import com.example.idp.web.dto.AssignRolesRequest;
import com.example.idp.web.dto.ClientResponse;
import com.example.idp.web.dto.CreateClientRequest;
import com.example.idp.web.dto.CreateUserRequest;
import com.example.idp.web.dto.PatchClientRequest;
import com.example.idp.web.dto.UserResponse;
import com.example.idp.web.dto.admin.AdminSessionRevokeRequest;
import com.example.idp.web.dto.admin.AuditLogResponse;
import com.example.idp.web.dto.admin.KeyResponse;
import com.example.idp.web.dto.admin.PagedResponse;
import com.example.idp.web.dto.admin.PermissionResponse;
import com.example.idp.web.dto.admin.RoleResponse;
import com.example.idp.web.dto.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final UserAdminService userAdminService;
    private final ClientAdminService clientAdminService;
    private final KeyManagementService keyManagementService;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public AdminController(UserAdminService userAdminService,
                           ClientAdminService clientAdminService,
                           KeyManagementService keyManagementService,
                           RefreshTokenSessionService refreshTokenSessionService,
                           RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           AuditLogRepository auditLogRepository,
                           AuditService auditService) {
        this.userAdminService = userAdminService;
        this.clientAdminService = clientAdminService;
        this.keyManagementService = keyManagementService;
        this.refreshTokenSessionService = refreshTokenSessionService;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    @GetMapping("/users")
    public java.util.List<UserResponse> listUsers() {
        return userAdminService.listUsers().stream().map(this::toUserResponse).toList();
    }

    @GetMapping("/clients")
    public List<ClientResponse> listClients() {
        return clientAdminService.listClients().stream().map(c -> toClientResponse(c, null)).toList();
    }

    @GetMapping("/roles")
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
            .map(role -> new RoleResponse(role.getId().toString(), role.getName(), role.getDescription()))
            .toList();
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
            .map(permission -> new PermissionResponse(permission.getId().toString(), permission.getName(), permission.getDescription()))
            .toList();
    }

    @GetMapping("/keys")
    public List<KeyResponse> listKeys() {
        return keyManagementService.listKeys().stream()
            .map(key -> new KeyResponse(key.getKid(), key.getAlgorithm(), key.getStatus().name(), key.getCreatedAt(), key.getRotatedAt()))
            .toList();
    }

    @GetMapping("/sessions")
    public PagedResponse<SessionResponse> listSessions(@org.springframework.web.bind.annotation.RequestParam(required = false) UUID userId,
                                                       @org.springframework.web.bind.annotation.RequestParam(required = false) String clientId,
                                                       @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
                                                       @org.springframework.web.bind.annotation.RequestParam(defaultValue = "50") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        var sessionPage = refreshTokenSessionService.listSessions(userId, clientId, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "issuedAt")));
        List<SessionResponse> items = sessionPage.getContent().stream()
            .map(s -> new SessionResponse(
                s.getId().toString(),
                s.getClientId(),
                s.getDeviceId(),
                s.getIssuedAt(),
                s.getExpiresAt(),
                s.getIp(),
                s.getUserAgent()))
            .toList();
        return new PagedResponse<>(items, sessionPage.getTotalElements(), safePage, safeSize);
    }

    @PostMapping("/sessions/revoke")
    public void revokeSession(@Valid @RequestBody AdminSessionRevokeRequest request,
                              HttpServletRequest httpRequest,
                              Authentication authentication) {
        UUID sessionId;
        try {
            sessionId = UUID.fromString(request.sessionId());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid session id");
        }
        boolean revoked = refreshTokenSessionService.revokeSessionById(sessionId);
        auditService.log(httpRequest, authentication, "ADMIN_SESSION_REVOKE", "session", request.sessionId(), revoked, Map.of());
    }

    @GetMapping("/audit-logs")
    public PagedResponse<AuditLogResponse> listAuditLogs(@org.springframework.web.bind.annotation.RequestParam(required = false) String eventType,
                                                         @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean success,
                                                         @org.springframework.web.bind.annotation.RequestParam(required = false) Instant from,
                                                         @org.springframework.web.bind.annotation.RequestParam(required = false) Instant to,
                                                         @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
                                                         @org.springframework.web.bind.annotation.RequestParam(defaultValue = "50") int size) {
        int safeSize = Math.min(Math.max(size, 1), 200);
        int safePage = Math.max(page, 0);
        var result = auditLogRepository.findAll((root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (eventType != null && !eventType.isBlank()) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }
            if (success != null) {
                predicates.add(cb.equal(root.get("success"), success));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<AuditLogResponse> items = result.getContent().stream()
            .map(log -> new AuditLogResponse(
                log.getId().toString(),
                log.getActorUserId() != null ? log.getActorUserId().toString() : null,
                log.getEventType(),
                log.getEntityType(),
                log.getEntityId(),
                log.isSuccess(),
                log.getIp(),
                log.getUserAgent(),
                log.getCorrelationId(),
                log.getDetails(),
                log.getCreatedAt()))
            .toList();
        return new PagedResponse<>(items, result.getTotalElements(), safePage, safeSize);
    }

    @PostMapping("/users")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request,
                                   HttpServletRequest httpRequest,
                                   Authentication authentication) {
        UserAdminService.CreatedUserResult result = userAdminService.createUser(
            request.email(), request.password(), request.emailVerified(),
            request.roles() == null ? Set.of("USER") : request.roles());
        auditService.log(httpRequest, authentication, "ADMIN_USER_CREATE", "user", result.user().getId().toString(), true,
            Map.of("email", result.user().getEmail()));
        return toUserResponse(result.user(), result.verificationToken());
    }

    @PostMapping("/users/{id}/roles")
    public void assignRoles(@PathVariable UUID id,
                            @Valid @RequestBody AssignRolesRequest request,
                            HttpServletRequest httpRequest,
                            Authentication authentication) {
        userAdminService.assignRoles(id, request.roles());
        auditService.log(httpRequest, authentication, "ADMIN_USER_ROLES_UPDATE", "user", id.toString(), true,
            Map.of("roles", request.roles()));
    }

    @PostMapping("/clients")
    public ClientResponse createClient(@Valid @RequestBody CreateClientRequest request,
                                       HttpServletRequest httpRequest,
                                       Authentication authentication) {
        ClientAdminService.ClientResult result = clientAdminService.createClient(
            request.clientId(),
            request.clientSecret(),
            request.name(),
            request.type(),
            defaultSet(request.redirectUris()),
            defaultSet(request.postLogoutRedirectUris()),
            defaultSet(request.grantTypes()),
            defaultSet(request.scopes()),
            request.requirePkce());

        auditService.log(httpRequest, authentication, "ADMIN_CLIENT_CREATE", "client", result.client().getClientId(), true, Map.of());
        return toClientResponse(result.client(), result.rawSecret());
    }

    @PatchMapping("/clients/{clientId}")
    public ClientResponse patchClient(@PathVariable String clientId,
                                      @RequestBody PatchClientRequest request,
                                      HttpServletRequest httpRequest,
                                      Authentication authentication) {
        ClientAdminService.ClientResult result = clientAdminService.patchClient(clientId, request.enabled(), request.rotateSecret());
        auditService.log(httpRequest, authentication, "ADMIN_CLIENT_UPDATE", "client", clientId, true,
            Map.of("enabled", request.enabled(), "rotated", request.rotateSecret()));
        return toClientResponse(result.client(), result.rawSecret());
    }

    @PostMapping("/keys/rotate")
    public Map<String, String> rotateKeys(HttpServletRequest httpRequest, Authentication authentication) {
        var key = keyManagementService.rotateKeys();
        auditService.log(httpRequest, authentication, "ADMIN_KEY_ROTATE", "jwk", key.getKid(), true, Map.of());
        return Map.of("kid", key.getKid(), "status", key.getStatus().name());
    }

    private UserResponse toUserResponse(UserEntity user) {
        return toUserResponse(user, null);
    }

    private UserResponse toUserResponse(UserEntity user, String verificationToken) {
        return new UserResponse(
            user.getId().toString(),
            user.getEmail(),
            user.isEmailVerified(),
            user.getStatus().name(),
            user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()),
            verificationToken
        );
    }

    private ClientResponse toClientResponse(OAuthClientEntity client, String rawSecret) {
        return new ClientResponse(
            client.getClientId(),
            client.getName(),
            client.getType(),
            client.isEnabled(),
            client.isRequirePkce(),
            Set.copyOf(Arrays.asList(client.getGrantTypes())),
            Set.copyOf(Arrays.asList(client.getScopes())),
            Set.copyOf(Arrays.asList(client.getRedirectUris())),
            rawSecret
        );
    }

    private Set<String> defaultSet(Set<String> input) {
        return input == null ? Set.of() : input;
    }
}
