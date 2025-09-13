package com.inventory.dto;

import com.inventory.security.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {

    private Long id;
    
    private String email;
    
    private String employeeCode;
    
    private String displayName;
    
    private TenantDto tenant;
    
    private Set<RoleDto> roles;
    
    private LocalDateTime lastLoginAt;
    
    private String accountStatus;

    public static UserInfoDto fromUserPrincipal(UserPrincipal principal) {
        // Convert role codes to RoleDto objects
        Set<RoleDto> roleDtos = principal.getRoles().stream()
                .map(roleCode -> RoleDto.builder()
                        .code(roleCode)
                        .name(getRoleName(roleCode))
                        .build())
                .collect(java.util.stream.Collectors.toSet());
        
        TenantDto tenantDto = TenantDto.builder()
                .id(principal.getTenantId())
                .name("Default Tenant") // You may want to fetch actual tenant name
                .build();

        return UserInfoDto.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .employeeCode(principal.getEmployeeCode())
                .displayName(principal.getDisplayName())
                .tenant(tenantDto)
                .roles(roleDtos)
                .accountStatus("ACTIVE")
                .build();
    }
    
    private static String getRoleName(String roleCode) {
        switch (roleCode) {
            case "ADMIN": return "Administrator";
            case "MANAGER": return "Store/Warehouse Manager";
            case "CLERK": return "Operations Clerk";
            case "VIEWER": return "Read-only";
            default: return roleCode;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDto {
        private Long id;
        private String code;
        private String name;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantDto {
        private Long id;
        private String code;
        private String name;
        private String status;
    }
}