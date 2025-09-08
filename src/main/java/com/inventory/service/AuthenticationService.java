package com.inventory.service;

import com.inventory.dto.*;
import com.inventory.entity.PasswordResetToken;
import com.inventory.entity.UserAccount;
import com.inventory.entity.UserSession;
import com.inventory.repository.PasswordResetTokenRepository;
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
import java.util.stream.Collectors;

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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

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
    public void initiatePasswordReset(String email, String ipAddress, String userAgent) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null) {
            // Don't reveal if email exists or not - but still log for security monitoring
            log.warn("Password reset requested for non-existent email: {} from IP: {}", email, ipAddress);
            return;
        }

        // Check for rate limiting
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentTokenCount = passwordResetTokenRepository.countRecentTokensByEmail(email, oneHourAgo);
        
        if (recentTokenCount >= 3) {
            log.warn("Password reset rate limit exceeded for email: {} from IP: {}", email, ipAddress);
            // Still don't reveal this to prevent enumeration
            return;
        }

        // Invalidate any existing tokens for this user
        passwordResetTokenRepository.invalidateUserTokens(user.getId(), LocalDateTime.now());

        // Generate new reset token
        String resetToken = passwordService.generateResetToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // 24 hour expiry

        // Create and save reset token
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setTenant(user.getTenant());
        passwordResetToken.setToken(resetToken);
        passwordResetToken.setEmail(email.toLowerCase());
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiresAt(expiresAt);
        passwordResetToken.setIpAddress(ipAddress);
        passwordResetToken.setUserAgent(userAgent);

        passwordResetTokenRepository.save(passwordResetToken);

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(email, user.getDisplayName(), resetToken, expiresAt);
            log.info("Password reset email sent successfully for email: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email for: {} - {}", email, e.getMessage());
            // Don't throw exception to prevent revealing if email exists
        }
    }

    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElse(null);

        return resetToken != null && resetToken.isValid();
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String ipAddress, String userAgent) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedAtIsNull(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            throw new BadCredentialsException("Reset token has expired");
        }

        UserAccount user = resetToken.getUser();

        // Validate new password policy
        PasswordService.PasswordValidationResult validation = 
                passwordService.validatePasswordPolicy(newPassword);
        
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Password does not meet policy requirements: " + 
                                             String.join(", ", validation.getErrors().stream()
                                                     .map(error -> error.getMessage())
                                                     .toList()));
        }

        // Check password reuse (allow if it's the same password - edge case)
        if (!passwordService.canReusePassword(user.getId(), newPassword)) {
            throw new IllegalArgumentException("Cannot reuse recent passwords");
        }

        // Update password
        String hashedPassword = passwordService.hashPassword(newPassword);
        user.setPasswordHash(hashedPassword.getBytes());
        user.setPasswordExpiresAt(passwordService.calculatePasswordExpiry());
        user.setMustChangePassword(false);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        
        userAccountRepository.save(user);

        // Save to password history
        passwordService.savePasswordHistory(user.getId(), hashedPassword);

        // Mark token as used
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        // Invalidate any other unused tokens for this user
        passwordResetTokenRepository.invalidateUserTokens(user.getId(), LocalDateTime.now());

        // Terminate all existing sessions to force re-login
        sessionService.terminateUserSessions(user.getId());

        // Send confirmation email
        try {
            emailService.sendPasswordChangedNotification(user.getEmail(), user.getDisplayName(), ipAddress, userAgent);
        } catch (Exception e) {
            log.warn("Failed to send password changed notification to: {} - {}", user.getEmail(), e.getMessage());
            // Don't throw exception for notification failure
        }

        log.info("Password reset completed successfully for user ID: {} via token", user.getId());
    }

    @Transactional(readOnly = true)
    public List<UserSessionDto> getUserSessions(Long userId) {
        return sessionService.getActiveUserSessions(userId).stream()
                .map(UserSessionDto::fromEntity)
                .collect(Collectors.toList());
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