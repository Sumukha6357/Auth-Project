package com.example.idp.service;

import com.example.idp.domain.EmailVerificationTokenEntity;
import com.example.idp.domain.RoleEntity;
import com.example.idp.domain.UserEntity;
import com.example.idp.repo.EmailVerificationTokenRepository;
import com.example.idp.repo.RoleRepository;
import com.example.idp.repo.UserRepository;
import com.example.idp.util.HashUtils;
import com.example.idp.util.TokenUtils;
import com.example.idp.web.ApiException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdpUserDetailsService userDetailsService;

    public UserAdminService(UserRepository userRepository,
                            RoleRepository roleRepository,
                            EmailVerificationTokenRepository emailTokenRepository,
                            PasswordEncoder passwordEncoder,
                            IdpUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailTokenRepository = emailTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Transactional(readOnly = true)
    public List<UserEntity> listUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public CreatedUserResult createUser(String email, String password, boolean emailVerified, Set<String> roleNames) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "User already exists");
        }
        UserEntity user = userDetailsService.createUser(email, passwordEncoder.encode(password), emailVerified);
        if (roleNames != null && !roleNames.isEmpty()) {
            assignRoles(user.getId(), roleNames);
            user = userRepository.findById(user.getId()).orElseThrow();
        }
        String verificationToken = null;
        if (!emailVerified) {
            verificationToken = createEmailVerificationToken(user.getId());
        }
        return new CreatedUserResult(user, verificationToken);
    }

    @Transactional
    public void assignRoles(UUID userId, Set<String> roleNames) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Set<RoleEntity> roles = roleNames.stream().map(name -> roleRepository.findByName(name)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Role not found: " + name))).collect(java.util.stream.Collectors.toSet());
        user.setRoles(roles);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        String hash = HashUtils.sha256(token);
        EmailVerificationTokenEntity tokenEntity = emailTokenRepository.findById(hash)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid verification token"));
        if (tokenEntity.getUsedAt() != null || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Expired or used verification token");
        }
        UserEntity user = userRepository.findById(tokenEntity.getUserId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        user.setEmailVerified(true);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        tokenEntity.setUsedAt(Instant.now());
        emailTokenRepository.save(tokenEntity);
        return true;
    }

    private String createEmailVerificationToken(UUID userId) {
        String token = TokenUtils.randomUrlSafeToken(32);
        EmailVerificationTokenEntity entity = new EmailVerificationTokenEntity();
        entity.setTokenHash(HashUtils.sha256(token));
        entity.setUserId(userId);
        entity.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        emailTokenRepository.save(entity);
        return token;
    }

    public record CreatedUserResult(UserEntity user, String verificationToken) {}
}
