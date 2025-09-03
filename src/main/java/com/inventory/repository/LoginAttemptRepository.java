package com.inventory.repository;

import com.inventory.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email AND la.attemptedAt >= :since " +
           "ORDER BY la.attemptedAt DESC")
    List<LoginAttempt> findRecentAttemptsByEmail(@Param("email") String email,
                                                 @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND " +
           "la.success = false AND la.attemptedAt >= :since")
    int countFailedAttemptsByEmailSince(@Param("email") String email,
                                        @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND " +
           "la.success = false AND la.attemptedAt >= :since")
    int countFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress,
                                     @Param("since") LocalDateTime since);

    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email AND la.success = false " +
           "ORDER BY la.attemptedAt DESC LIMIT 1")
    LoginAttempt findLastFailedAttemptByEmail(@Param("email") String email);

    void deleteByAttemptedAtBefore(LocalDateTime cutoffDate);

    @Query("SELECT la FROM LoginAttempt la WHERE la.attemptedAt >= :since AND " +
           "la.success = false GROUP BY la.ipAddress HAVING COUNT(la) >= :threshold")
    List<LoginAttempt> findSuspiciousIpAddresses(@Param("since") LocalDateTime since,
                                                  @Param("threshold") int threshold);
}