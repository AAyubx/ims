package com.inventory.controller;

import com.inventory.dto.*;
import com.inventory.security.UserPrincipal;
import com.inventory.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and session management")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            LoginResponse response = authenticationService.login(loginRequest, ipAddress, userAgent);
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
            
        } catch (Exception e) {
            log.warn("Login failed for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication failed", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate user session")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest request) {
        
        try {
            String sessionId = request.getHeader("X-Session-Id");
            authenticationService.logout(sessionId, currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
            
        } catch (Exception e) {
            log.error("Logout failed for user ID: {} - {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Logout failed", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        
        try {
            LoginResponse response = authenticationService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
            
        } catch (Exception e) {
            log.warn("Token refresh failed - {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Token refresh failed", e.getMessage()));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Get authenticated user information")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserInfoDto>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        UserInfoDto userInfo = UserInfoDto.fromUserPrincipal(currentUser);
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        try {
            // Validate password confirmation
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password confirmation does not match"));
            }
            
            authenticationService.changePassword(currentUser.getId(), request);
            
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
            
        } catch (Exception e) {
            log.error("Password change failed for user ID: {} - {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password change failed", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Initiate password reset process")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            authenticationService.initiatePasswordReset(request.getEmail(), ipAddress, userAgent);
            
            // Always return success to prevent email enumeration
            return ResponseEntity.ok(ApiResponse.success(
                    "If the email exists, a password reset link has been sent", null));
            
        } catch (Exception e) {
            log.error("Password reset initiation failed - {}", e.getMessage());
            
            // Still return success to prevent email enumeration
            return ResponseEntity.ok(ApiResponse.success(
                    "If the email exists, a password reset link has been sent", null));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Validate password confirmation
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password confirmation does not match"));
            }
            
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            authenticationService.resetPassword(request.getToken(), request.getNewPassword(), ipAddress, userAgent);
            
            return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
            
        } catch (Exception e) {
            log.error("Password reset failed - {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password reset failed", e.getMessage()));
        }
    }

    @PostMapping("/validate-token")
    @Operation(summary = "Validate reset token", description = "Validate password reset token")
    public ResponseEntity<ApiResponse<Void>> validateResetToken(
            @RequestBody ValidateTokenRequest request) {
        
        try {
            boolean isValid = authenticationService.validateResetToken(request.getToken());
            
            if (isValid) {
                return ResponseEntity.ok(ApiResponse.success("Token is valid", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid or expired token"));
            }
            
        } catch (Exception e) {
            log.error("Token validation failed - {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid token", e.getMessage()));
        }
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get user sessions", description = "Get active sessions for current user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<UserSessionDto>>> getUserSessions(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        try {
            List<UserSessionDto> sessions = authenticationService.getUserSessions(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(sessions));
            
        } catch (Exception e) {
            log.error("Failed to get sessions for user ID: {} - {}", currentUser.getId(), e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve sessions", e.getMessage()));
        }
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Terminate session", description = "Terminate a specific user session")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> terminateSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        try {
            authenticationService.terminateUserSession(currentUser.getId(), sessionId);
            return ResponseEntity.ok(ApiResponse.success("Session terminated", null));
            
        } catch (Exception e) {
            log.error("Failed to terminate session {} for user ID: {} - {}", 
                     sessionId, currentUser.getId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to terminate session", e.getMessage()));
        }
    }

    @DeleteMapping("/sessions")
    @Operation(summary = "Terminate all sessions", description = "Terminate all user sessions")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> terminateAllSessions(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        try {
            authenticationService.terminateAllUserSessions(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("All sessions terminated", null));
            
        } catch (Exception e) {
            log.error("Failed to terminate all sessions for user ID: {} - {}", 
                     currentUser.getId(), e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to terminate sessions", e.getMessage()));
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    // Additional DTOs for this controller
    public static class RefreshTokenRequest {
        private String refreshToken;
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class ValidateTokenRequest {
        private String token;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}