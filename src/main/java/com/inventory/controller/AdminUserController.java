package com.inventory.controller;

import com.inventory.dto.*;
import com.inventory.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - User Management", description = "Administrative user management operations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List all users", description = "Get paginated list of all users in the tenant")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir,
            
            @Parameter(description = "Search term (email, name, or employee code)")
            @RequestParam(required = false) String search) {
        
        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                               Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<UserResponseDto> users;
            if (search != null && !search.trim().isEmpty()) {
                users = adminUserService.searchUsers(search.trim(), pageable);
            } else {
                users = adminUserService.getAllUsers(pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(users));
            
        } catch (Exception e) {
            log.error("Failed to retrieve users - {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve users", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get detailed user information by ID")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        try {
            UserResponseDto user = adminUserService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(user));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to retrieve user ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve user", e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<UserCreationResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        
        try {
            UserResponseDto user = adminUserService.createUser(request);
            
            // Create response with generated password (if applicable)
            UserCreationResponse response = UserCreationResponse.builder()
                    .user(user)
                    .message(request.getInitialPassword() == null ? 
                            "User created successfully. Generated password will be sent separately." :
                            "User created successfully.")
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("User created successfully", response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User creation failed", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create user - {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("User creation failed", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update existing user information")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        
        try {
            UserResponseDto user = adminUserService.updateUser(id, request);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User update failed", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update user ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("User update failed", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate user", description = "Deactivate a user account (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        try {
            adminUserService.deactivateUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User deactivation failed", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to deactivate user ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("User deactivation failed", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset user password", description = "Generate and set new password for user")
    public ResponseEntity<ApiResponse<PasswordResetResponse>> resetUserPassword(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        try {
            String newPassword = adminUserService.resetUserPassword(id);
            
            PasswordResetResponse response = PasswordResetResponse.builder()
                    .message("Password reset successfully. New password generated.")
                    .temporaryPassword(newPassword)
                    .mustChangeOnNextLogin(true)
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully", response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password reset failed", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to reset password for user ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Password reset failed", e.getMessage()));
        }
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock user account", description = "Unlock a locked user account")
    public ResponseEntity<ApiResponse<Void>> unlockUserAccount(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        try {
            adminUserService.unlockUserAccount(id);
            return ResponseEntity.ok(ApiResponse.success("User account unlocked successfully", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Account unlock failed", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to unlock account for user ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Account unlock failed", e.getMessage()));
        }
    }

    @GetMapping("/{id}/sessions")
    @Operation(summary = "Get user sessions", description = "Get active sessions for a specific user")
    public ResponseEntity<ApiResponse<List<UserSessionDto>>> getUserSessions(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        try {
            List<UserSessionDto> sessions = adminUserService.getUserSessions(id);
            return ResponseEntity.ok(ApiResponse.success(sessions));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve sessions", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to retrieve sessions for user ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve sessions", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/sessions/{sessionId}")
    @Operation(summary = "Terminate user session", description = "Terminate a specific session for a user")
    public ResponseEntity<ApiResponse<Void>> terminateUserSession(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Parameter(description = "Session ID")
            @PathVariable String sessionId) {
        
        try {
            adminUserService.terminateUserSession(id, sessionId);
            return ResponseEntity.ok(ApiResponse.success("Session terminated successfully", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Session termination failed", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to terminate session {} for user ID: {} - {}", sessionId, id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Session termination failed", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/sessions")
    @Operation(summary = "Terminate all user sessions", description = "Terminate all active sessions for a user")
    public ResponseEntity<ApiResponse<Void>> terminateAllUserSessions(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        try {
            adminUserService.terminateAllUserSessions(id);
            return ResponseEntity.ok(ApiResponse.success("All sessions terminated successfully", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Session termination failed", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to terminate all sessions for user ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Session termination failed", e.getMessage()));
        }
    }

    // Response DTOs specific to this controller
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserCreationResponse {
        private UserResponseDto user;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PasswordResetResponse {
        private String message;
        private String temporaryPassword;
        private boolean mustChangeOnNextLogin;
    }
}