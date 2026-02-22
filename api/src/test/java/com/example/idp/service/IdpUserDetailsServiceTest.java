package com.example.idp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.idp.config.IdpProperties;
import com.example.idp.domain.PermissionEntity;
import com.example.idp.domain.RoleEntity;
import com.example.idp.domain.UserEntity;
import com.example.idp.domain.UserStatus;
import com.example.idp.repo.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

class IdpUserDetailsServiceTest {
    private UserRepository userRepository;
    private IdpUserDetailsService service;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        IdpProperties props = new IdpProperties();
        props.getLockout().setMaxFailures(5);
        props.getLockout().setLockMinutes(15);
        service = new IdpUserDetailsService(userRepository, props);
    }

    @Test
    void lockoutAfterFiveFailures() {
        UserEntity user = baseUser();
        user.setFailedAttempts(4);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.onAuthenticationFailure("user@example.com");

        assertThat(user.getFailedAttempts()).isEqualTo(5);
        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(user.getLockedUntil()).isAfter(Instant.now());
    }

    @Test
    void mapsRolesAndPermissions() {
        UserEntity user = baseUser();
        RoleEntity role = new RoleEntity();
        role.setId(UUID.randomUUID());
        role.setName("ADMIN");
        PermissionEntity permission = new PermissionEntity();
        permission.setId(UUID.randomUUID());
        permission.setName("admin:full_access");
        role.setPermissions(Set.of(permission));
        user.setRoles(Set.of(role));

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user@example.com");

        assertThat(details.getAuthorities()).extracting("authority").contains("admin:full_access");
    }

    private UserEntity baseUser() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPasswordHash("hash");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedAttempts(0);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }
}
