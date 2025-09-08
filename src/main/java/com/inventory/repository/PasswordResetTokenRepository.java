package com.inventory.repository;

import com.inventory.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndUsedAtIsNull(String token);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.email = :email AND prt.usedAt IS NULL " +
           "AND prt.expiresAt > :currentTime ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findValidTokensByEmail(@Param("email") String email, 
                                                   @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user.id = :userId AND prt.usedAt IS NULL " +
           "AND prt.expiresAt > :currentTime ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findValidTokensByUserId(@Param("userId") Long userId, 
                                                    @Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.usedAt = :usedAt WHERE prt.user.id = :userId " +
           "AND prt.usedAt IS NULL")
    int invalidateUserTokens(@Param("userId") Long userId, @Param("usedAt") LocalDateTime usedAt);

    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.usedAt = :usedAt WHERE prt.email = :email " +
           "AND prt.usedAt IS NULL")
    int invalidateEmailTokens(@Param("email") String email, @Param("usedAt") LocalDateTime usedAt);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :cutoffTime")
    int deleteExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.email = :email " +
           "AND prt.createdAt > :since")
    long countRecentTokensByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.ipAddress = :ipAddress " +
           "AND prt.createdAt > :since")
    long countRecentTokensByIpAddress(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    boolean existsByEmailAndUsedAtIsNullAndExpiresAtAfter(String email, LocalDateTime currentTime);
}