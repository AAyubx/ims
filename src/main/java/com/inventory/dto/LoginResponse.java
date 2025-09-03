package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    
    private String refreshToken;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private long expiresIn;
    
    private String sessionId;
    
    private UserInfoDto userInfo;
    
    @Builder.Default
    private boolean mustChangePassword = false;
    
    private LocalDateTime passwordExpiresAt;
}