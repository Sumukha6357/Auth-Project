package com.example.idp.service;

import com.example.idp.config.IdpProperties;
import com.example.idp.domain.PermissionEntity;
import com.example.idp.domain.RoleEntity;
import com.example.idp.domain.UserEntity;
import com.example.idp.domain.UserStatus;
import com.example.idp.repo.UserRepository;
import com.example.idp.security.IdpUserPrincipal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdpUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final IdpProperties idpProperties;

    public IdpUserDetailsService(UserRepository userRepository, IdpProperties idpProperties) {
        this.userRepository = userRepository;
        this.idpProperties = idpProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (isLocked(user)) {
            throw new LockedException("Account locked");
        }

        Set<String> roles = user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(PermissionEntity::getName)
            .collect(Collectors.toSet());

        return new IdpUserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getStatus() == UserStatus.ACTIVE,
            !isLocked(user),
            user.isEmailVerified(),
            roles,
            permissions
        );
    }

    @Transactional
    public void onAuthenticationFailure(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            int failures = user.getFailedAttempts() + 1;
            user.setFailedAttempts(failures);
            if (failures >= idpProperties.getLockout().getMaxFailures()) {
                user.setLockedUntil(Instant.now().plus(idpProperties.getLockout().getLockMinutes(), ChronoUnit.MINUTES));
                user.setStatus(UserStatus.LOCKED);
            }
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void onAuthenticationSuccess(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            if (user.getStatus() == UserStatus.LOCKED) {
                user.setStatus(UserStatus.ACTIVE);
            }
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public UserEntity createUser(String email, String passwordHash, boolean emailVerified) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email.toLowerCase());
        user.setPasswordHash(passwordHash);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(emailVerified);
        user.setFailedAttempts(0);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    private boolean isLocked(UserEntity user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now());
    }
}
