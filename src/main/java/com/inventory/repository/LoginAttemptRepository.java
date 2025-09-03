package com.inventory.repository;

import com.inventory.entity.LoginAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

       List<LoginAttempt> findByEmailAndAttemptedAtAfterOrderByAttemptedAtDesc(String email, LocalDateTime since);

       int countByEmailAndSuccessFalseAndAttemptedAtAfter(String email, LocalDateTime since);

       int countByIpAddressAndSuccessFalseAndAttemptedAtAfter(String ipAddress, LocalDateTime since);

       // Use Pageable to get the last failed attempt
       LoginAttempt findFirstByEmailAndSuccessFalseOrderByAttemptedAtDesc(String email);

       void deleteByAttemptedAtBefore(LocalDateTime cutoffDate);

       @Query("SELECT la FROM LoginAttempt la WHERE la.attemptedAt >= :since AND " +
                     "la.success = false GROUP BY la.ipAddress HAVING COUNT(la) >= :threshold")
       List<LoginAttempt> findSuspiciousIpAddresses(@Param("since") LocalDateTime since,
                     @Param("threshold") int threshold);
}