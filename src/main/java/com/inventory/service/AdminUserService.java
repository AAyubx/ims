package com.inventory.service;

import com.inventory.dto.CreateUserRequest;
import com.inventory.dto.UpdateUserRequest;
import com.inventory.dto.UserResponseDto;
import com.inventory.dto.UserSessionDto;
import com.inventory.entity.Role;
import com.inventory.entity.Tenant;
import com.inventory.entity.UserAccount;
import com.inventory.repository.RoleRepository;
import com.inventory.repository.TenantRepository;
import com.inventory.repository.UserAccountRepository;
import com.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordService passwordService;
    private final SessionService sessionService;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Long tenantId = getCurrentTenantId();
        
        return userAccountRepository.findByTenantId(tenantId, pageable)
                .map(UserResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> searchUsers(String searchTerm, Pageable pageable) {
        Long tenantId = getCurrentTenantId();
        
        return userAccountRepository.findByTenantIdAndSearchTerm(tenantId, searchTerm, pageable)
                .map(UserResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        Long tenantId = getCurrentTenantId();
        
        UserAccount user = userAccountRepository.findById(id)
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    public UserResponseDto createUser(CreateUserRequest request) {
        Long tenantId = getCurrentTenantId();
        Long currentUserId = getCurrentUserId();
        
        // Validate unique constraints
        if (userAccountRepository.existsByEmailIgnoreCaseAndTenantId(request.getEmail(), tenantId)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        if (userAccountRepository.existsByEmployeeCodeAndTenantId(request.getEmployeeCode(), tenantId)) {
            throw new IllegalArgumentException("Employee code already exists");
        }

        // Validate roles exist
        Set<Role> roles = roleRepository.findAllById(request.getRoleIds())
                .stream()
                .collect(java.util.stream.Collectors.toSet());
        
        if (roles.size() != request.getRoleIds().size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }

        // Get tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        // Create user
        UserAccount user = new UserAccount();
        user.setTenant(tenant);
        user.setEmail(request.getEmail().toLowerCase());
        user.setEmployeeCode(request.getEmployeeCode());
        user.setDisplayName(request.getDisplayName());
        user.setStatus(UserAccount.UserStatus.ACTIVE);
        user.setRoles(roles);
        user.setMustChangePassword(request.isMustChangePassword());

        // Set created by
        UserAccount createdBy = new UserAccount();
        createdBy.setId(currentUserId);
        user.setCreatedBy(createdBy);
        user.setUpdatedBy(createdBy);

        // Handle password
        String password = request.getInitialPassword();
        if (password == null || password.trim().isEmpty()) {
            password = passwordService.generateSecurePassword();
            user.setMustChangePassword(true);
        } else {
            // Validate password policy
            PasswordService.PasswordValidationResult validation = 
                    passwordService.validatePasswordPolicy(password);
            
            if (!validation.isValid()) {
                throw new IllegalArgumentException("Password does not meet policy requirements: " + 
                                                 validation.getErrors());
            }
        }

        String hashedPassword = passwordService.hashPassword(password);
        user.setPasswordHash(hashedPassword.getBytes());
        user.setPasswordExpiresAt(passwordService.calculatePasswordExpiry());

        UserAccount savedUser = userAccountRepository.save(user);

        // Save password history
        passwordService.savePasswordHistory(savedUser.getId(), hashedPassword);

        log.info("Created user with ID: {} by admin user ID: {}", savedUser.getId(), currentUserId);

        return UserResponseDto.fromEntity(savedUser);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequest request) {
        Long tenantId = getCurrentTenantId();
        Long currentUserId = getCurrentUserId();

        UserAccount user = userAccountRepository.findById(id)
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isUpdated = false;

        // Update display name
        if (request.getDisplayName() != null && 
            !request.getDisplayName().equals(user.getDisplayName())) {
            user.setDisplayName(request.getDisplayName());
            isUpdated = true;
        }

        // Update employee code (if different)
        if (request.getEmployeeCode() != null && 
            !request.getEmployeeCode().equals(user.getEmployeeCode())) {
            
            if (userAccountRepository.existsByEmployeeCodeAndTenantId(
                    request.getEmployeeCode(), tenantId)) {
                throw new IllegalArgumentException("Employee code already exists");
            }
            
            user.setEmployeeCode(request.getEmployeeCode());
            isUpdated = true;
        }

        // Update roles
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> newRoles = roleRepository.findAllById(request.getRoleIds())
                    .stream()
                    .collect(java.util.stream.Collectors.toSet());
            
            if (newRoles.size() != request.getRoleIds().size()) {
                throw new IllegalArgumentException("One or more roles not found");
            }
            
            // Check if we're removing admin role from the last admin
            if (user.hasRole("ADMIN") && !newRoles.stream().anyMatch(role -> role.getCode().equals("ADMIN"))) {
                long adminCount = userAccountRepository.countActiveAdmins();
                if (adminCount <= 1) {
                    throw new IllegalArgumentException("Cannot remove admin role from the last active admin");
                }
            }
            
            user.setRoles(newRoles);
            isUpdated = true;
        }

        // Update status
        if (request.getStatus() != null && request.getStatus() != user.getStatus()) {
            // Check if we're deactivating the last admin
            if (request.getStatus() == UserAccount.UserStatus.INACTIVE && user.hasRole("ADMIN")) {
                long adminCount = userAccountRepository.countActiveAdmins();
                if (adminCount <= 1) {
                    throw new IllegalArgumentException("Cannot deactivate the last active admin");
                }
            }
            
            user.setStatus(request.getStatus());
            
            // If deactivating, terminate all sessions
            if (request.getStatus() == UserAccount.UserStatus.INACTIVE) {
                sessionService.terminateUserSessions(user.getId());
            }
            
            isUpdated = true;
        }

        // Update must change password flag
        if (request.getMustChangePassword() != null && 
            request.getMustChangePassword() != user.isMustChangePassword()) {
            user.setMustChangePassword(request.getMustChangePassword());
            isUpdated = true;
        }

        if (isUpdated) {
            UserAccount updatedBy = new UserAccount();
            updatedBy.setId(currentUserId);
            user.setUpdatedBy(updatedBy);
            
            userAccountRepository.save(user);
            log.info("Updated user ID: {} by admin user ID: {}", id, currentUserId);
        }

        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    public void deactivateUser(Long id) {
        Long tenantId = getCurrentTenantId();
        Long currentUserId = getCurrentUserId();

        UserAccount user = userAccountRepository.findById(id)
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if we're deactivating the last admin
        if (user.hasRole("ADMIN")) {
            long adminCount = userAccountRepository.countActiveAdmins();
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Cannot deactivate the last active admin");
            }
        }

        user.setStatus(UserAccount.UserStatus.INACTIVE);
        
        UserAccount updatedBy = new UserAccount();
        updatedBy.setId(currentUserId);
        user.setUpdatedBy(updatedBy);
        
        userAccountRepository.save(user);

        // Terminate all user sessions
        sessionService.terminateUserSessions(user.getId());

        log.info("Deactivated user ID: {} by admin user ID: {}", id, currentUserId);
    }

    @Transactional
    public String resetUserPassword(Long id) {
        Long tenantId = getCurrentTenantId();
        Long currentUserId = getCurrentUserId();

        UserAccount user = userAccountRepository.findById(id)
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate new password
        String newPassword = passwordService.generateSecurePassword();
        String hashedPassword = passwordService.hashPassword(newPassword);

        // Update user
        user.setPasswordHash(hashedPassword.getBytes());
        user.setPasswordExpiresAt(passwordService.calculatePasswordExpiry());
        user.setMustChangePassword(true);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        
        UserAccount updatedBy = new UserAccount();
        updatedBy.setId(currentUserId);
        user.setUpdatedBy(updatedBy);
        
        userAccountRepository.save(user);

        // Save password history
        passwordService.savePasswordHistory(user.getId(), hashedPassword);

        // Terminate all user sessions
        sessionService.terminateUserSessions(user.getId());

        log.info("Reset password for user ID: {} by admin user ID: {}", id, currentUserId);

        return newPassword; // Return the generated password (should be sent securely to user)
    }

    @Transactional
    public void unlockUserAccount(Long id) {
        Long tenantId = getCurrentTenantId();
        Long currentUserId = getCurrentUserId();

        UserAccount user = userAccountRepository.findById(id)
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        
        UserAccount updatedBy = new UserAccount();
        updatedBy.setId(currentUserId);
        user.setUpdatedBy(updatedBy);
        
        userAccountRepository.save(user);

        log.info("Unlocked account for user ID: {} by admin user ID: {}", id, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<UserSessionDto> getUserSessions(Long id) {
        Long tenantId = getCurrentTenantId();

        // Verify user exists and belongs to tenant
        userAccountRepository.findById(id)
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return sessionService.getActiveUserSessions(id).stream()
                .map(UserSessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void terminateUserSession(Long userId, String sessionId) {
        Long tenantId = getCurrentTenantId();
        Long currentUserId = getCurrentUserId();

        // Verify user exists and belongs to tenant
        userAccountRepository.findById(userId)
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        sessionService.invalidateSession(sessionId);
        
        log.info("Admin user ID: {} terminated session {} for user ID: {}", 
                currentUserId, sessionId, userId);
    }

    @Transactional
    public void terminateAllUserSessions(Long id) {
        Long tenantId = getCurrentTenantId();
        Long currentUserId = getCurrentUserId();

        // Verify user exists and belongs to tenant
        userAccountRepository.findById(id)
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int terminated = sessionService.terminateUserSessions(id);
        
        log.info("Admin user ID: {} terminated {} sessions for user ID: {}", 
                currentUserId, terminated, id);
    }

    private Long getCurrentTenantId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getTenantId();
    }

    private Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getId();
    }
}