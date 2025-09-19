package com.inventory.security;

import com.inventory.entity.Role;
import com.inventory.entity.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private Long tenantId;
    private String email;
    private String employeeCode;
    private String displayName;
    private String password;
    private UserAccount.UserStatus status;
    private boolean mustChangePassword;
    private LocalDateTime passwordExpiresAt;
    private int failedLoginAttempts;
    private LocalDateTime accountLockedUntil;
    private LocalDateTime lastLoginAt;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(UserAccount user) {
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                .collect(Collectors.toSet());

        return new UserPrincipal(
                user.getId(),
                user.getTenant().getId(),
                user.getEmail(),
                user.getEmployeeCode(),
                user.getDisplayName(),
                user.getPasswordHash() != null ? new String(user.getPasswordHash()) : null,
                user.getStatus(),
                user.isMustChangePassword(),
                user.getPasswordExpiresAt(),
                user.getFailedLoginAttempts(),
                user.getAccountLockedUntil(),
                user.getLastLoginAt(),
                authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return status == UserAccount.UserStatus.ACTIVE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountLockedUntil == null || accountLockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return passwordExpiresAt == null || passwordExpiresAt.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return status == UserAccount.UserStatus.ACTIVE;
    }

    public boolean hasRole(String roleCode) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleCode));
    }

    public boolean hasAnyRole(String... roleCodes) {
        for (String roleCode : roleCodes) {
            if (hasRole(roleCode)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .collect(Collectors.toSet());
    }

    public boolean isPasswordExpired() {
        return passwordExpiresAt != null && passwordExpiresAt.isBefore(LocalDateTime.now());
    }
}