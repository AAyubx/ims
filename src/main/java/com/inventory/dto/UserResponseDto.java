package com.inventory.dto;

import com.inventory.entity.Role;
import com.inventory.entity.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    
    private String employeeCode;
    
    private String email;
    
    private String displayName;
    
    private UserAccount.UserStatus status;
    
    private Set<RoleDto> roles;
    
    private int failedLoginAttempts;
    
    private LocalDateTime accountLockedUntil;
    
    private LocalDateTime lastLoginAt;
    
    private LocalDateTime passwordExpiresAt;
    
    private boolean mustChangePassword;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    public static UserResponseDto fromEntity(UserAccount user) {
        Set<RoleDto> roles = user.getRoles() != null ? 
                user.getRoles().stream()
                        .map(RoleDto::fromEntity)
                        .collect(Collectors.toSet()) : 
                Set.of();

        return UserResponseDto.builder()
                .id(user.getId())
                .employeeCode(user.getEmployeeCode())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .roles(roles)
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .accountLockedUntil(user.getAccountLockedUntil())
                .lastLoginAt(user.getLastLoginAt())
                .passwordExpiresAt(user.getPasswordExpiresAt())
                .mustChangePassword(user.isMustChangePassword())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleDto {
        private Long id;
        private String code;
        private String name;

        public static RoleDto fromEntity(Role role) {
            return RoleDto.builder()
                    .id(role.getId())
                    .code(role.getCode())
                    .name(role.getName())
                    .build();
        }
    }
}