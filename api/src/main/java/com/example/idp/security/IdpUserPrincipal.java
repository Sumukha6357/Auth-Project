package com.example.idp.security;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class IdpUserPrincipal implements UserDetails {
    private final UUID userId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final boolean emailVerified;
    private final Set<String> roles;
    private final Set<String> permissions;

    public IdpUserPrincipal(UUID userId,
                            String username,
                            String password,
                            boolean enabled,
                            boolean accountNonLocked,
                            boolean emailVerified,
                            Set<String> roles,
                            Set<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.emailVerified = emailVerified;
        this.roles = roles;
        this.permissions = permissions;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
