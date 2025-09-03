package com.inventory.repository;

import com.inventory.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndActionTypeOrderByCreatedAtDesc(Long tenantId,
                                                                   AuditLog.ActionType actionType,
                                                                   Pageable pageable);

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.tenant.id = :tenantId AND " +
           "al.entityType = :entityType AND al.entityId = :entityId " +
           "ORDER BY al.createdAt DESC")
    Page<AuditLog> findByTenantIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            @Param("tenantId") Long tenantId,
            @Param("entityType") String entityType,
            @Param("entityId") String entityId,
            Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.tenant.id = :tenantId AND " +
           "al.createdAt >= :startDate AND al.createdAt <= :endDate " +
           "ORDER BY al.createdAt DESC")
    Page<AuditLog> findByTenantIdAndCreatedAtBetween(@Param("tenantId") Long tenantId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId AND " +
           "al.actionType = :actionType AND al.createdAt >= :since")
    List<AuditLog> findRecentActionsByUser(@Param("userId") Long userId,
                                           @Param("actionType") AuditLog.ActionType actionType,
                                           @Param("since") LocalDateTime since);

    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.tenant.id = :tenantId AND " +
           "al.actionType = :actionType AND al.createdAt >= :since")
    long countActionsByTenantSince(@Param("tenantId") Long tenantId,
                                   @Param("actionType") AuditLog.ActionType actionType,
                                   @Param("since") LocalDateTime since);
}