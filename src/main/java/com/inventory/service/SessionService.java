package com.inventory.service;

import com.inventory.entity.SystemConfig;
import com.inventory.entity.Tenant;
import com.inventory.entity.UserAccount;
import com.inventory.entity.UserSession;
import com.inventory.repository.SystemConfigRepository;
import com.inventory.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final SystemConfigRepository systemConfigRepository;

    @Transactional
    public UserSession createSession(Long userId, Long tenantId, String ipAddress, String userAgent) {
        // Generate unique session ID
        String sessionId = UUID.randomUUID().toString();
        
        // Get session timeout from configuration
        int timeoutMinutes = getConfigValue(SystemConfig.ConfigKey.SESSION_TIMEOUT_MINUTES.getKey(), 480);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(timeoutMinutes);

        // Create session entity
        UserSession session = new UserSession();
        session.setId(sessionId);
        
        UserAccount user = new UserAccount();
        user.setId(userId);
        session.setUser(user);
        
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        session.setTenant(tenant);
        
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setExpiresAt(expiresAt);
        session.setActive(true);

        UserSession savedSession = sessionRepository.save(session);
        
        log.debug("Created session {} for user ID: {}, expires at: {}", 
                 sessionId, userId, expiresAt);
        
        return savedSession;
    }

    @Transactional
    public void updateSessionActivity(String sessionId) {
        sessionRepository.findByIdAndIsActiveTrue(sessionId)
                .ifPresent(session -> {
                    session.setLastAccessedAt(LocalDateTime.now());
                    sessionRepository.save(session);
                    log.debug("Updated last accessed time for session: {}", sessionId);
                });
    }

    @Transactional
    public void invalidateSession(String sessionId) {
        int updated = sessionRepository.deactivateSession(sessionId);
        if (updated > 0) {
            log.info("Invalidated session: {}", sessionId);
        } else {
            log.warn("Attempted to invalidate non-existent session: {}", sessionId);
        }
    }

    @Transactional(readOnly = true)
    public boolean isSessionValid(String sessionId) {
        return sessionRepository.findByIdAndIsActiveTrue(sessionId)
                .map(session -> !session.isExpired())
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public UserSession getSessionInfo(String sessionId) {
        return sessionRepository.findByIdAndIsActiveTrue(sessionId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserSession> getActiveUserSessions(Long userId) {
        return sessionRepository.findByUserIdAndIsActiveTrueOrderByLastAccessedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public int getActiveSessionCount(Long userId) {
        return sessionRepository.countActiveSessionsByUserId(userId);
    }

    @Transactional
    public int terminateUserSessions(Long userId) {
        int terminated = sessionRepository.deactivateAllUserSessions(userId);
        log.info("Terminated {} sessions for user ID: {}", terminated, userId);
        return terminated;
    }

    @Transactional
    public void terminateInactiveSessions(Long userId, int inactiveMinutes) {
        LocalDateTime inactiveThreshold = LocalDateTime.now().minusMinutes(inactiveMinutes);
        
        List<UserSession> inactiveSessions = sessionRepository
                .findInactiveSessionsByUserId(userId, inactiveThreshold);
        
        for (UserSession session : inactiveSessions) {
            session.setActive(false);
        }
        
        if (!inactiveSessions.isEmpty()) {
            sessionRepository.saveAll(inactiveSessions);
            log.info("Terminated {} inactive sessions for user ID: {}", 
                    inactiveSessions.size(), userId);
        }
    }

    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find and mark expired sessions as inactive
        List<UserSession> expiredSessions = sessionRepository.findExpiredSessions(now);
        
        for (UserSession session : expiredSessions) {
            session.setActive(false);
        }
        
        if (!expiredSessions.isEmpty()) {
            sessionRepository.saveAll(expiredSessions);
            log.info("Marked {} expired sessions as inactive", expiredSessions.size());
        }
        
        // Delete old inactive sessions (older than 7 days)
        LocalDateTime cutoffTime = now.minusDays(7);
        int deleted = sessionRepository.deleteExpiredAndInactiveSessions(cutoffTime);
        
        if (deleted > 0) {
            log.info("Deleted {} old inactive sessions", deleted);
        }
    }

    @Transactional
    public boolean enforceSessionLimit(Long userId) {
        int maxSessions = 3; // Default max concurrent sessions
        
        List<UserSession> activeSessions = getActiveUserSessions(userId);
        
        if (activeSessions.size() >= maxSessions) {
            // Terminate the oldest session
            UserSession oldestSession = activeSessions.get(activeSessions.size() - 1);
            invalidateSession(oldestSession.getId());
            
            log.info("Enforced session limit for user ID: {}, terminated oldest session: {}", 
                    userId, oldestSession.getId());
            
            return true;
        }
        
        return false;
    }

    @Transactional(readOnly = true)
    public boolean isUserSessionLimitReached(Long userId) {
        int maxSessions = 3; // Default max concurrent sessions
        return getActiveSessionCount(userId) >= maxSessions;
    }

    private int getConfigValue(String key, int defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(config -> Integer.parseInt(config.getConfigValue()))
                .orElse(defaultValue);
    }
}