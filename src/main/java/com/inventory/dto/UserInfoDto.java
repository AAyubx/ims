package com.inventory.dto;

import com.inventory.security.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    private Long tenantId;
    
    private Set<String> roles;

    public static UserInfoDto fromUserPrincipal(UserPrincipal principal) {
        return UserInfoDto.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .employeeCode(principal.getEmployeeCode())
                .displayName(principal.getDisplayName())
                .tenantId(principal.getTenantId())
                .roles(principal.getRoles())
                .build();
    }
}