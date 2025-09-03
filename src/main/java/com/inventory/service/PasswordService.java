package com.inventory.service;

import com.inventory.entity.SystemConfig;
import com.inventory.entity.UserAccount;
import com.inventory.entity.UserPasswordHistory;
import com.inventory.repository.SystemConfigRepository;
import com.inventory.repository.UserPasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final UserPasswordHistoryRepository passwordHistoryRepository;
    private final SystemConfigRepository systemConfigRepository;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    // Password character sets
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public PasswordValidationResult validatePasswordPolicy(String password) {
        PasswordValidationResult result = new PasswordValidationResult();
        
        if (password == null || password.isEmpty()) {
            result.addError(PasswordValidationError.EMPTY_PASSWORD);
            return result;
        }

        // Get password policy configuration
        int minLength = getConfigValue(SystemConfig.ConfigKey.PASSWORD_MIN_LENGTH.getKey(), 8);
        boolean requireUppercase = getConfigValue(SystemConfig.ConfigKey.PASSWORD_REQUIRE_UPPERCASE.getKey(), true);
        boolean requireLowercase = getConfigValue(SystemConfig.ConfigKey.PASSWORD_REQUIRE_LOWERCASE.getKey(), true);
        boolean requireDigit = getConfigValue(SystemConfig.ConfigKey.PASSWORD_REQUIRE_DIGIT.getKey(), true);
        boolean requireSpecial = getConfigValue(SystemConfig.ConfigKey.PASSWORD_REQUIRE_SPECIAL.getKey(), true);

        // Check length
        if (password.length() < minLength) {
            result.addError(PasswordValidationError.TOO_SHORT);
        }
        
        if (password.length() > 128) {
            result.addError(PasswordValidationError.TOO_LONG);
        }

        // Check character requirements
        if (requireUppercase && !Pattern.compile("[A-Z]").matcher(password).find()) {
            result.addError(PasswordValidationError.MISSING_UPPERCASE);
        }

        if (requireLowercase && !Pattern.compile("[a-z]").matcher(password).find()) {
            result.addError(PasswordValidationError.MISSING_LOWERCASE);
        }

        if (requireDigit && !Pattern.compile("[0-9]").matcher(password).find()) {
            result.addError(PasswordValidationError.MISSING_DIGIT);
        }

        if (requireSpecial && !Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]").matcher(password).find()) {
            result.addError(PasswordValidationError.MISSING_SPECIAL_CHAR);
        }

        // Calculate password strength
        result.setStrength(calculatePasswordStrength(password));

        return result;
    }

    @Transactional(readOnly = true)
    public boolean canReusePassword(Long userId, String newPassword) {
        if (userId == null) return true;

        int historyCount = getConfigValue(SystemConfig.ConfigKey.PASSWORD_HISTORY_COUNT.getKey(), 3);
        
        List<UserPasswordHistory> recentPasswords = passwordHistoryRepository
                .findRecentPasswordsByUserId(userId, historyCount);

        String hashedNewPassword = hashPassword(newPassword);
        
        return recentPasswords.stream()
                .noneMatch(history -> passwordEncoder.matches(newPassword, 
                          new String(history.getPasswordHash())));
    }

    @Transactional
    public void savePasswordHistory(Long userId, String hashedPassword) {
        if (userId == null) return;

        UserAccount user = new UserAccount();
        user.setId(userId);

        UserPasswordHistory history = new UserPasswordHistory();
        history.setUser(user);
        history.setPasswordHash(hashedPassword.getBytes());
        
        passwordHistoryRepository.save(history);
        log.debug("Saved password history for user ID: {}", userId);
    }

    public LocalDateTime calculatePasswordExpiry() {
        int expiryDays = getConfigValue(SystemConfig.ConfigKey.PASSWORD_EXPIRY_DAYS.getKey(), 60);
        return LocalDateTime.now().plusDays(expiryDays);
    }

    public boolean isPasswordExpired(LocalDateTime passwordExpiresAt) {
        return passwordExpiresAt != null && passwordExpiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isPasswordExpiringSoon(LocalDateTime passwordExpiresAt, int warningDays) {
        if (passwordExpiresAt == null) return false;
        
        LocalDateTime warningDate = LocalDateTime.now().plusDays(warningDays);
        return passwordExpiresAt.isBefore(warningDate);
    }

    public String generateSecurePassword() {
        int length = 12; // Default secure length
        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each required set
        password.append(getRandomChar(UPPERCASE));
        password.append(getRandomChar(LOWERCASE));
        password.append(getRandomChar(DIGITS));
        password.append(getRandomChar(SPECIAL_CHARS));

        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(getRandomChar(ALL_CHARS));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    public String generateResetToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        
        StringBuilder token = new StringBuilder();
        for (byte b : tokenBytes) {
            token.append(String.format("%02x", b));
        }
        
        return token.toString();
    }

    private char getRandomChar(String chars) {
        return chars.charAt(secureRandom.nextInt(chars.length()));
    }

    private String shuffleString(String string) {
        char[] chars = string.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;
        
        // Length bonus
        if (password.length() >= 8) strength += 20;
        if (password.length() >= 12) strength += 10;
        if (password.length() >= 16) strength += 10;

        // Character diversity
        if (Pattern.compile("[a-z]").matcher(password).find()) strength += 15;
        if (Pattern.compile("[A-Z]").matcher(password).find()) strength += 15;
        if (Pattern.compile("[0-9]").matcher(password).find()) strength += 15;
        if (Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]").matcher(password).find()) strength += 15;

        // Complexity bonus
        int uniqueChars = (int) password.chars().distinct().count();
        if (uniqueChars >= 8) strength += 10;

        return Math.min(100, strength);
    }

    private int getConfigValue(String key, int defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(config -> Integer.parseInt(config.getConfigValue()))
                .orElse(defaultValue);
    }

    private boolean getConfigValue(String key, boolean defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(config -> Boolean.parseBoolean(config.getConfigValue()))
                .orElse(defaultValue);
    }

    public static class PasswordValidationResult {
        private boolean valid = true;
        private final List<PasswordValidationError> errors = new java.util.ArrayList<>();
        private int strength = 0;

        public boolean isValid() {
            return valid && errors.isEmpty();
        }

        public List<PasswordValidationError> getErrors() {
            return errors;
        }

        public int getStrength() {
            return strength;
        }

        public void setStrength(int strength) {
            this.strength = strength;
        }

        void addError(PasswordValidationError error) {
            this.errors.add(error);
            this.valid = false;
        }
    }

    public enum PasswordValidationError {
        EMPTY_PASSWORD("Password cannot be empty"),
        TOO_SHORT("Password is too short"),
        TOO_LONG("Password is too long"),
        MISSING_UPPERCASE("Password must contain at least one uppercase letter"),
        MISSING_LOWERCASE("Password must contain at least one lowercase letter"),
        MISSING_DIGIT("Password must contain at least one digit"),
        MISSING_SPECIAL_CHAR("Password must contain at least one special character"),
        CONTAINS_EMAIL("Password cannot contain email address"),
        CONTAINS_USERNAME("Password cannot contain username"),
        REUSED_PASSWORD("Password has been used recently"),
        WEAK_PASSWORD("Password is too weak");

        private final String message;

        PasswordValidationError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}