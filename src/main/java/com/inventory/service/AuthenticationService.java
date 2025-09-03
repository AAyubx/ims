package com.inventory.service;

import com.inventory.dto.*;
import com.inventory.entity.UserAccount;
import com.inventory.entity.UserSession;
import com.inventory.repository.UserAccountRepository;
import com.inventory.security.JwtTokenProvider;
import com.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SessionService sessionService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordService passwordService;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        String email = loginRequest.getEmail().toLowerCase();
        
        try {
            // Check if account is locked
            if (loginAttemptService.isAccountLocked(email)) {
                LocalDateTime lockoutExpiry = loginAttemptService.getLockoutExpiry(email);
                loginAttemptService.recordLoginAttempt(email, ipAddress, userAgent, 
                                                     false, "Account locked");
                throw new LockedException("Account is locked until " + lockoutExpiry);
            }

            // Check for suspicious activity
            loginAttemptService.checkSuspiciousActivity(email, ipAddress);

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword())
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Additional security checks
            if (!userPrincipal.isEnabled()) {
                loginAttemptService.recordLoginAttempt(email, ipAddress, userAgent, 
                                                     false, "Account disabled");
                throw new DisabledException("Account is disabled");
            }

            if (userPrincipal.isPasswordExpired()) {
                // Allow login but force password change
                log.info("User {} logged in with expired password", email);
            }

            // Enforce session limits
            if (sessionService.isUserSessionLimitReached(userPrincipal.getId())) {
                sessionService.enforceSessionLimit(userPrincipal.getId());
            }

            // Create session
            UserSession session = sessionService.createSession(
                userPrincipal.getId(), 
                userPrincipal.getTenantId(),
                ipAddress, 
                userAgent
            );

            // Generate JWT tokens
            String accessToken = tokenProvider.generateAccessToken(userPrincipal);
            String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

            // Update user's last login time and reset failed attempts
            updateUserLoginInfo(userPrincipal.getId(), ipAddress);

            // Record successful login attempt
            loginAttemptService.recordLoginAttempt(email, ipAddress, userAgent, true, null);

            log.info("User {} successfully logged in from IP: {}", email, ipAddress);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationTimeInSeconds())
                    .sessionId(session.getId())
                    .userInfo(UserInfoDto.fromUserPrincipal(userPrincipal))
                    .mustChangePassword(userPrincipal.isMustChangePassword() || userPrincipal.isPasswordExpired())
                    .passwordExpiresAt(userPrincipal.getPasswordExpiresAt())
                    .build();

        } catch (AuthenticationException e) {
            String failureReason = determineFailureReason(e);
            loginAttemptService.recordLoginAttempt(email, ipAddress, userAgent, false, failureReason);
            
            log.warn("Failed login attempt for email: {} from IP: {} - Reason: {}", 
                    email, ipAddress, failureReason);
            
            throw e;
        }
    }

    @Transactional
    public void logout(String sessionId, Long userId) {
        if (sessionId != null) {
            sessionService.invalidateSession(sessionId);
            log.info("User ID {} logged out, session {} invalidated", userId, sessionId);
        }
    }

    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        if (!tokenProvider.isTokenValid(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        UserPrincipal userPrincipal = UserPrincipal.create(user);

        // Generate new access token
        String newAccessToken = tokenProvider.generateAccessToken(userPrincipal);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Keep the same refresh token
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTimeInSeconds())
                .userInfo(UserInfoDto.fromUserPrincipal(userPrincipal))
                .mustChangePassword(userPrincipal.isMustChangePassword())
                .passwordExpiresAt(userPrincipal.getPasswordExpiresAt())
                .build();
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate current password
        if (!passwordService.validatePassword(request.getCurrentPassword(), 
                                            new String(user.getPasswordHash()))) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Validate new password policy
        PasswordService.PasswordValidationResult validation = 
                passwordService.validatePasswordPolicy(request.getNewPassword());
        
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Password does not meet policy requirements: " + 
                                             validation.getErrors());
        }

        // Check password reuse
        if (!passwordService.canReusePassword(userId, request.getNewPassword())) {
            throw new IllegalArgumentException("Cannot reuse recent passwords");
        }

        // Update password
        String hashedPassword = passwordService.hashPassword(request.getNewPassword());
        user.setPasswordHash(hashedPassword.getBytes());
        user.setPasswordExpiresAt(passwordService.calculatePasswordExpiry());
        user.setMustChangePassword(false);
        
        userAccountRepository.save(user);

        // Save to password history
        passwordService.savePasswordHistory(userId, hashedPassword);

        // Terminate all existing sessions to force re-login
        sessionService.terminateUserSessions(userId);

        log.info("Password changed successfully for user ID: {}", userId);
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null) {
            // Don't reveal if email exists or not
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        String resetToken = passwordService.generateResetToken();
        // In a real implementation, you would:
        // 1. Store the reset token with expiry in database
        // 2. Send email with reset link
        
        log.info("Password reset initiated for email: {}", email);
        // TODO: Implement email sending and token storage
    }

    @Transactional(readOnly = true)
    public List<UserSessionDto> getUserSessions(Long userId) {
        return sessionService.getActiveUserSessions(userId).stream()
                .map(UserSessionDto::fromEntity)
                .toList();
    }

    @Transactional
    public void terminateUserSession(Long userId, String sessionId) {
        // Verify the session belongs to the user
        UserSession session = sessionService.getSessionInfo(sessionId);
        if (session != null && session.getUser().getId().equals(userId)) {
            sessionService.invalidateSession(sessionId);
            log.info("User ID {} terminated their session {}", userId, sessionId);
        } else {
            throw new IllegalArgumentException("Session not found or does not belong to user");
        }
    }

    @Transactional
    public void terminateAllUserSessions(Long userId) {
        int terminated = sessionService.terminateUserSessions(userId);
        log.info("User ID {} terminated all their sessions ({})", userId, terminated);
    }

    private void updateUserLoginInfo(Long userId, String ipAddress) {
        userAccountRepository.findById(userId)
                .ifPresent(user -> {
                    user.setLastLoginAt(LocalDateTime.now());
                    user.setFailedLoginAttempts(0);
                    user.setAccountLockedUntil(null);
                    userAccountRepository.save(user);
                });
    }

    private String determineFailureReason(AuthenticationException e) {
        if (e instanceof BadCredentialsException) {
            return "Invalid credentials";
        } else if (e instanceof LockedException) {
            return "Account locked";
        } else if (e instanceof DisabledException) {
            return "Account disabled";
        } else {
            return "Authentication failed";
        }
    }

}