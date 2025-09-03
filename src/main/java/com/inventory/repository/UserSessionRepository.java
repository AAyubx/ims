package com.inventory.repository;

import com.inventory.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    Optional<UserSession> findByIdAndIsActiveTrue(String id);

    List<UserSession> findByUserIdAndIsActiveTrueOrderByLastAccessedAtDesc(Long userId);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user.id = :userId AND s.isActive = true")
    int countActiveSessionsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user.id = :userId")
    int deactivateAllUserSessions(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.id = :sessionId")
    int deactivateSession(@Param("sessionId") String sessionId);

    @Query("SELECT s FROM UserSession s WHERE s.expiresAt <= :currentTime")
    List<UserSession> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt <= :cutoffTime OR s.isActive = false")
    int deleteExpiredAndInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.isActive = true AND " +
           "s.lastAccessedAt < :inactiveThreshold")
    List<UserSession> findInactiveSessionsByUserId(@Param("userId") Long userId,
                                                   @Param("inactiveThreshold") LocalDateTime inactiveThreshold);
}