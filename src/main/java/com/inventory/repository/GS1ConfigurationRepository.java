package com.inventory.repository;

import com.inventory.entity.GS1Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GS1ConfigurationRepository extends JpaRepository<GS1Configuration, Long> {

    /**
     * Find all active configurations for a tenant
     */
    List<GS1Configuration> findByTenantIdAndIsActiveTrueOrderByGs1PrefixAsc(Long tenantId);

    /**
     * Find configuration by GS1 prefix for tenant
     */
    Optional<GS1Configuration> findByTenantIdAndGs1Prefix(Long tenantId, String gs1Prefix);

    /**
     * Find active configurations with available capacity
     */
    @Query("SELECT gc FROM GS1Configuration gc WHERE gc.tenantId = :tenantId AND gc.isActive = true AND gc.nextSequence <= gc.prefixCapacity")
    List<GS1Configuration> findAvailableConfigurations(@Param("tenantId") Long tenantId);

    /**
     * Find configurations running low on capacity (>80% utilized)
     */
    @Query("SELECT gc FROM GS1Configuration gc WHERE gc.tenantId = :tenantId AND gc.isActive = true AND (CAST(gc.nextSequence - 1 AS double) / gc.prefixCapacity) > 0.8")
    List<GS1Configuration> findLowCapacityConfigurations(@Param("tenantId") Long tenantId);

    /**
     * Find configurations critically low on capacity (>95% utilized)
     */
    @Query("SELECT gc FROM GS1Configuration gc WHERE gc.tenantId = :tenantId AND gc.isActive = true AND (CAST(gc.nextSequence - 1 AS double) / gc.prefixCapacity) > 0.95")
    List<GS1Configuration> findCriticalCapacityConfigurations(@Param("tenantId") Long tenantId);

    /**
     * Find configurations that are exhausted (no more capacity)
     */
    @Query("SELECT gc FROM GS1Configuration gc WHERE gc.tenantId = :tenantId AND gc.nextSequence > gc.prefixCapacity")
    List<GS1Configuration> findExhaustedConfigurations(@Param("tenantId") Long tenantId);

    /**
     * Find all configurations for tenant with pagination
     */
    Page<GS1Configuration> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    /**
     * Get capacity statistics for tenant
     */
    @Query("SELECT " +
           "COUNT(gc), " +
           "SUM(gc.prefixCapacity), " +
           "SUM(gc.nextSequence - 1), " +
           "AVG(CAST(gc.nextSequence - 1 AS double) / gc.prefixCapacity * 100) " +
           "FROM GS1Configuration gc " +
           "WHERE gc.tenantId = :tenantId AND gc.isActive = true")
    Object[] getCapacityStatisticsForTenant(@Param("tenantId") Long tenantId);

    /**
     * Check if GS1 prefix already exists for tenant (excluding specific ID)
     */
    @Query("SELECT COUNT(gc) > 0 FROM GS1Configuration gc WHERE gc.tenantId = :tenantId AND gc.gs1Prefix = :prefix AND (:id IS NULL OR gc.id != :id)")
    boolean existsByTenantIdAndGs1PrefixExcludingId(@Param("tenantId") Long tenantId, @Param("prefix") String prefix, @Param("id") Long id);

    /**
     * Find best configuration for GTIN generation (active with most remaining capacity)
     */
    @Query("SELECT gc FROM GS1Configuration gc WHERE gc.tenantId = :tenantId AND gc.isActive = true AND gc.nextSequence <= gc.prefixCapacity ORDER BY (gc.prefixCapacity - gc.nextSequence + 1) DESC")
    Optional<GS1Configuration> findBestConfigurationForGeneration(@Param("tenantId") Long tenantId);

    /**
     * Update next sequence number (for atomic GTIN generation)
     */
    @Query("UPDATE GS1Configuration gc SET gc.nextSequence = gc.nextSequence + 1 WHERE gc.id = :id AND gc.nextSequence <= gc.prefixCapacity")
    int incrementSequence(@Param("id") Long id);
}