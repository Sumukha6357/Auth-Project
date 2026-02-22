package com.example.idp.bootstrap;

import com.example.idp.config.IdpProperties;
import com.example.idp.domain.PermissionEntity;
import com.example.idp.domain.RoleEntity;
import com.example.idp.domain.UserEntity;
import com.example.idp.repo.PermissionRepository;
import com.example.idp.repo.RoleRepository;
import com.example.idp.repo.UserRepository;
import com.example.idp.service.UserAdminService;
import com.example.idp.util.SecretResolver;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapService {
    private final IdpProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserAdminService userAdminService;
    private final SecretResolver secretResolver;

    public BootstrapService(IdpProperties properties,
                            UserRepository userRepository,
                            RoleRepository roleRepository,
                            PermissionRepository permissionRepository,
                            UserAdminService userAdminService,
                            SecretResolver secretResolver) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userAdminService = userAdminService;
        this.secretResolver = secretResolver;
    }

    @PostConstruct
    @Transactional
    public void bootstrap() {
        ensureAdminRole();
        String adminEmail = secretResolver.resolve(properties.getBootstrap().getAdminEmail(), "IDP_ADMIN_BOOTSTRAP_EMAIL");
        String adminPassword = secretResolver.resolve(properties.getBootstrap().getAdminPassword(), "IDP_ADMIN_BOOTSTRAP_PASSWORD");

        if (adminEmail == null || adminPassword == null || adminEmail.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException("Bootstrap admin credentials are required");
        }

        if (userRepository.findAll().stream().noneMatch(u -> u.getEmail().equalsIgnoreCase(adminEmail))) {
            UserAdminService.CreatedUserResult result = userAdminService.createUser(
                adminEmail,
                adminPassword,
                true,
                Set.of("ADMIN")
            );
            UserEntity user = result.user();
            userAdminService.assignRoles(user.getId(), Set.of("ADMIN"));
        }
    }

    private void ensureAdminRole() {
        PermissionEntity adminPermission = permissionRepository.findByName("admin:full_access").orElseGet(() -> {
            PermissionEntity p = new PermissionEntity();
            p.setId(UUID.randomUUID());
            p.setName("admin:full_access");
            p.setDescription("Full administrative access");
            return permissionRepository.save(p);
        });

        RoleEntity adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            RoleEntity role = new RoleEntity();
            role.setId(UUID.randomUUID());
            role.setName("ADMIN");
            role.setDescription("Platform admin");
            return roleRepository.save(role);
        });

        if (adminRole.getPermissions().stream().noneMatch(p -> p.getName().equals(adminPermission.getName()))) {
            adminRole.getPermissions().add(adminPermission);
            roleRepository.save(adminRole);
        }
    }
}
