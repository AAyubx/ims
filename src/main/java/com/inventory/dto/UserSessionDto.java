package com.inventory.dto;

import com.inventory.entity.UserSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionDto {

    private String id;
    
    private String ipAddress;
    
    private String userAgent;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastAccessedAt;
    
    private LocalDateTime expiresAt;
    
    private boolean active;

    public static UserSessionDto fromEntity(UserSession session) {
        return UserSessionDto.builder()
                .id(session.getId())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .createdAt(session.getCreatedAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .expiresAt(session.getExpiresAt())
                .active(session.isActive())
                .build();
    }
}