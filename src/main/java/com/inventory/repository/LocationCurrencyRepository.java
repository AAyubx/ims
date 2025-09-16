package com.inventory.repository;

import com.inventory.entity.LocationCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationCurrencyRepository extends JpaRepository<LocationCurrency, Long> {

    /**
     * Find all currencies for a location
     */
    List<LocationCurrency> findByLocationId(Long locationId);

    /**
     * Find all currencies for a tenant
     */
    List<LocationCurrency> findByTenant_Id(Long tenantId);

    /**
     * Find currency configuration for a specific location and currency
     */
    Optional<LocationCurrency> findByLocationIdAndCurrencyCode(Long locationId, String currencyCode);

    /**
     * Find primary currency for a location
     */
    Optional<LocationCurrency> findByLocationIdAndIsPrimaryTrue(Long locationId);

    /**
     * Find all locations using a specific currency
     */
    List<LocationCurrency> findByTenant_IdAndCurrencyCode(Long tenantId, String currencyCode);

    /**
     * Check if a currency is already configured for a location
     */
    boolean existsByLocationIdAndCurrencyCode(Long locationId, String currencyCode);

    /**
     * Count primary currencies for a location (should be 0 or 1)
     */
    long countByLocationIdAndIsPrimaryTrue(Long locationId);

    /**
     * Find locations without a primary currency (data quality check)
     */
    @Query("SELECT DISTINCT lc.location.id FROM LocationCurrency lc " +
           "WHERE lc.tenant.id = :tenantId " +
           "AND lc.location.id NOT IN (" +
           "  SELECT lc2.location.id FROM LocationCurrency lc2 " +
           "  WHERE lc2.tenant.id = :tenantId AND lc2.isPrimary = true" +
           ")")
    List<Long> findLocationIdsWithoutPrimaryCurrency(@Param("tenantId") Long tenantId);
}