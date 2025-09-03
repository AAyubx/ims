package com.inventory.service;

import com.inventory.entity.LoginAttempt;
import com.inventory.entity.SystemConfig;
import com.inventory.repository.LoginAttemptRepository;
import com.inventory.repository.SystemConfigRepository;
import com.inventory.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final UserAccountRepository userAccountRepository;
    private final SystemConfigRepository systemConfigRepository;

    @Transactional
    public void recordLoginAttempt(String email, String ipAddress, String userAgent,
            boolean success, String failureReason) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email.toLowerCase());
        attempt.setIpAddress(ipAddress);
        attempt.setUserAgent(userAgent);
        attempt.setSuccess(success);
        attempt.setFailureReason(failureReason);

        loginAttemptRepository.save(attempt);

        log.info("Recorded login attempt for email: {}, IP: {}, success: {}",
                email, ipAddress, success);

        if (!success) {
            checkAndLockAccount(email);
        }
    }

    @Transactional(readOnly = true)
    public int getFailedAttemptCount(String email) {
        int lockoutMinutes = getConfigValue(SystemConfig.ConfigKey.LOGIN_LOCKOUT_MINUTES.getKey(), 30);
        LocalDateTime since = LocalDateTime.now().minusMinutes(lockoutMinutes);

        return loginAttemptRepository.countByEmailAndSuccessFalseAndAttemptedAtAfter(email.toLowerCase(), since);
    }

    @Transactional(readOnly = true)
    public boolean isAccountLocked(String email) {
        return userAccountRepository.findByEmailIgnoreCase(email)
                .map(user -> user.getAccountLockedUntil() != null &&
                        user.getAccountLockedUntil().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public LocalDateTime getLockoutExpiry(String email) {
        return userAccountRepository.findByEmailIgnoreCase(email)
                .map(user -> user.getAccountLockedUntil())
                .orElse(null);
    }

    @Transactional
    public void unlockAccount(String email) {
        userAccountRepository.findByEmailIgnoreCase(email)
                .ifPresent(user -> {
                    user.setAccountLockedUntil(null);
                    user.setFailedLoginAttempts(0);
                    userAccountRepository.save(user);
                    log.info("Unlocked account for email: {}", email);
                });
    }

    @Transactional(readOnly = true)
    public List<LoginAttempt> getRecentFailedAttempts(String email, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return loginAttemptRepository.findByEmailAndAttemptedAtAfterOrderByAttemptedAtDesc(email.toLowerCase(), since)
                .stream()
                .filter(attempt -> !attempt.isSuccess())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void checkSuspiciousActivity(String email, String ipAddress) {
        int threshold = 20; // Suspicious activity threshold
        LocalDateTime since = LocalDateTime.now().minusHours(1);

        int failedAttemptsFromIp = loginAttemptRepository
                .countByIpAddressAndSuccessFalseAndAttemptedAtAfter(ipAddress, since);

        if (failedAttemptsFromIp >= threshold) {
            log.warn("Suspicious activity detected from IP: {} - {} failed attempts in last hour",
                    ipAddress, failedAttemptsFromIp);
            // Here you could trigger additional security measures
        }
    }

    @Transactional
    public void cleanupOldAttempts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        loginAttemptRepository.deleteByAttemptedAtBefore(cutoffDate);
        log.info("Cleaned up login attempts older than 30 days");
    }

    private void checkAndLockAccount(String email) {
        userAccountRepository.findByEmailIgnoreCase(email)
                .ifPresent(user -> {
                    int maxAttempts = getConfigValue(SystemConfig.ConfigKey.LOGIN_MAX_ATTEMPTS.getKey(), 5);
                    int lockoutMinutes = getConfigValue(SystemConfig.ConfigKey.LOGIN_LOCKOUT_MINUTES.getKey(), 30);

                    int failedAttempts = user.getFailedLoginAttempts() + 1;
                    user.setFailedLoginAttempts(failedAttempts);

                    if (failedAttempts >= maxAttempts) {
                        LocalDateTime lockoutExpiry = LocalDateTime.now().plusMinutes(lockoutMinutes);
                        user.setAccountLockedUntil(lockoutExpiry);

                        log.warn("Account locked for email: {} after {} failed attempts. " +
                                "Lockout expires at: {}", email, failedAttempts, lockoutExpiry);
                    }

                    userAccountRepository.save(user);
                });
    }

    private int getConfigValue(String key, int defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(config -> Integer.parseInt(config.getConfigValue()))
                .orElse(defaultValue);
    }
}