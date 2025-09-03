package com.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.entity.AuditLog;
import com.inventory.entity.Tenant;
import com.inventory.entity.UserAccount;
import com.inventory.repository.AuditLogRepository;
import com.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(AuditLog.ActionType actionType, String entityType, String entityId) {
        logAction(actionType, entityType, entityId, null, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(AuditLog.ActionType actionType, String entityType, String entityId, 
                         Object oldValues, Object newValues) {
        
        try {
            AuditLog auditLog = createAuditLog(actionType, entityType, entityId, oldValues, newValues);
            auditLogRepository.save(auditLog);
            
            log.debug("Audit log created for action: {} on entity: {} (ID: {})", 
                     actionType, entityType, entityId);
            
        } catch (Exception e) {
            log.error("Failed to create audit log for action: {} on entity: {} (ID: {}) - {}", 
                     actionType, entityType, entityId, e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserAction(Long userId, AuditLog.ActionType actionType, String entityType, 
                             String entityId, Object oldValues, Object newValues) {
        
        try {
            AuditLog auditLog = createAuditLogForUser(userId, actionType, entityType, entityId, 
                                                     oldValues, newValues);
            auditLogRepository.save(auditLog);
            
            log.debug("Audit log created for user {} action: {} on entity: {} (ID: {})", 
                     userId, actionType, entityType, entityId);
            
        } catch (Exception e) {
            log.error("Failed to create audit log for user {} action: {} on entity: {} (ID: {}) - {}", 
                     userId, actionType, entityType, entityId, e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginAttempt(String email, boolean success, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog();
            
            // Set minimal required fields for login attempts
            auditLog.setActionType(success ? AuditLog.ActionType.LOGIN : AuditLog.ActionType.LOGIN);
            auditLog.setEntityType("LOGIN_ATTEMPT");
            auditLog.setEntityId(email);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            
            // For login attempts, we might not have full tenant/user context yet
            // Try to get tenant from security context if available
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
                Tenant tenant = new Tenant();
                tenant.setId(userPrincipal.getTenantId());
                auditLog.setTenant(tenant);
                
                if (success) {
                    UserAccount user = new UserAccount();
                    user.setId(userPrincipal.getId());
                    auditLog.setUser(user);
                }
            }
            
            // Add result information
            Map<String, Object> resultData = Map.of(
                "success", success,
                "email", email,
                "timestamp", LocalDateTime.now()
            );
            
            auditLog.setNewValues(objectMapper.writeValueAsString(resultData));
            
            auditLogRepository.save(auditLog);
            
            log.debug("Audit log created for login attempt: {} for email: {}", 
                     success ? "SUCCESS" : "FAILURE", email);
            
        } catch (Exception e) {
            log.error("Failed to create audit log for login attempt - {}", e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPasswordChange(Long userId, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = createAuditLogForUser(userId, AuditLog.ActionType.UPDATE, 
                                                     "USER_PASSWORD", userId.toString(), 
                                                     null, Map.of("action", "password_changed"));
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            
            auditLogRepository.save(auditLog);
            
            log.debug("Audit log created for password change for user ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to create audit log for password change - {}", e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAdminAction(Long adminUserId, Long targetUserId, AuditLog.ActionType actionType, 
                              String description, Object oldValues, Object newValues) {
        try {
            AuditLog auditLog = createAuditLogForUser(adminUserId, AuditLog.ActionType.ADMIN_ACTION, 
                                                     "USER_MANAGEMENT", targetUserId.toString(), 
                                                     oldValues, newValues);
            
            // Add admin action details
            Map<String, Object> adminData = Map.of(
                "adminUserId", adminUserId,
                "targetUserId", targetUserId,
                "action", actionType.toString(),
                "description", description
            );
            
            String existingNewValues = auditLog.getNewValues();
            if (existingNewValues != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existingData = objectMapper.readValue(existingNewValues, Map.class);
                existingData.putAll(adminData);
                auditLog.setNewValues(objectMapper.writeValueAsString(existingData));
            } else {
                auditLog.setNewValues(objectMapper.writeValueAsString(adminData));
            }
            
            auditLogRepository.save(auditLog);
            
            log.debug("Audit log created for admin action by user {} on user {}: {}", 
                     adminUserId, targetUserId, description);
            
        } catch (Exception e) {
            log.error("Failed to create audit log for admin action - {}", e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanupOldAuditLogs(int retentionDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
            
            log.info("Cleaned up audit logs older than {} days", retentionDays);
            
        } catch (Exception e) {
            log.error("Failed to cleanup old audit logs - {}", e.getMessage());
        }
    }

    private AuditLog createAuditLog(AuditLog.ActionType actionType, String entityType, String entityId,
                                   Object oldValues, Object newValues) {
        
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(actionType);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        
        // Get current user and tenant from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            
            UserAccount user = new UserAccount();
            user.setId(userPrincipal.getId());
            auditLog.setUser(user);
            
            Tenant tenant = new Tenant();
            tenant.setId(userPrincipal.getTenantId());
            auditLog.setTenant(tenant);
            
            // Get session ID if available
            auditLog.setSessionId(getSessionIdFromRequest());
        }
        
        // Get request information
        setRequestInfo(auditLog);
        
        // Set old and new values
        setAuditValues(auditLog, oldValues, newValues);
        
        return auditLog;
    }

    private AuditLog createAuditLogForUser(Long userId, AuditLog.ActionType actionType, 
                                          String entityType, String entityId,
                                          Object oldValues, Object newValues) {
        
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(actionType);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        
        // Set user
        UserAccount user = new UserAccount();
        user.setId(userId);
        auditLog.setUser(user);
        
        // Get tenant from security context or user context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            Tenant tenant = new Tenant();
            tenant.setId(userPrincipal.getTenantId());
            auditLog.setTenant(tenant);
            
            auditLog.setSessionId(getSessionIdFromRequest());
        }
        
        // Get request information
        setRequestInfo(auditLog);
        
        // Set old and new values
        setAuditValues(auditLog, oldValues, newValues);
        
        return auditLog;
    }

    private void setRequestInfo(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            log.debug("Could not get request information for audit log - {}", e.getMessage());
        }
    }

    private String getSessionIdFromRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("X-Session-Id");
            }
        } catch (Exception e) {
            log.debug("Could not get session ID from request - {}", e.getMessage());
        }
        return null;
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

    private void setAuditValues(AuditLog auditLog, Object oldValues, Object newValues) {
        try {
            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            
            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit values - {}", e.getMessage());
        }
    }
}