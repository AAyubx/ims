package com.inventory.repository;

import com.inventory.entity.Location;
import com.inventory.entity.Location.LocationStatus;
import com.inventory.entity.Location.LocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Find all locations for a tenant
     */
    Page<Location> findByTenant_IdAndStatusNot(Long tenantId, LocationStatus status, Pageable pageable);

    /**
     * Find location by tenant and code
     */
    Optional<Location> findByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Find locations by tenant and type
     */
    List<Location> findByTenant_IdAndTypeAndStatus(Long tenantId, LocationType type, LocationStatus status);

    /**
     * Find locations by country
     */
    List<Location> findByTenant_IdAndCountryCodeAndStatus(Long tenantId, String countryCode, LocationStatus status);

    /**
     * Find locations by parent location (hierarchy)
     */
    List<Location> findByParentLocationIdAndStatus(Long parentLocationId, LocationStatus status);

    /**
     * Find top-level locations (no parent)
     */
    List<Location> findByTenant_IdAndParentLocationIsNullAndStatus(Long tenantId, LocationStatus status);

    /**
     * Find locations managed by a specific user
     */
    List<Location> findByStoreManagerIdAndStatus(Long storeManagerId, LocationStatus status);

    /**
     * Find stores within a geographical radius using Haversine formula
     * This is a simple proximity query - for production use, consider using spatial extensions
     */
    @Query("SELECT l FROM Location l WHERE l.tenant.id = :tenantId " +
           "AND l.latitude IS NOT NULL AND l.longitude IS NOT NULL " +
           "AND l.status = :status " +
           "AND (6371 * ACOS(COS(RADIANS(:lat)) * COS(RADIANS(l.latitude)) * " +
           "COS(RADIANS(l.longitude) - RADIANS(:lng)) + " +
           "SIN(RADIANS(:lat)) * SIN(RADIANS(l.latitude)))) <= :radiusKm")
    List<Location> findLocationsWithinRadius(@Param("tenantId") Long tenantId,
                                           @Param("lat") BigDecimal latitude,
                                           @Param("lng") BigDecimal longitude,
                                           @Param("radiusKm") Double radiusKm,
                                           @Param("status") LocationStatus status);

    /**
     * Check if location code exists for tenant
     */
    boolean existsByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Count locations by type for a tenant
     */
    long countByTenant_IdAndTypeAndStatus(Long tenantId, LocationType type, LocationStatus status);

    /**
     * Find locations by city for a tenant
     */
    List<Location> findByTenant_IdAndCityAndStatus(Long tenantId, String city, LocationStatus status);

    /**
     * Find direct children of a location
     */
    @Query("SELECT l FROM Location l WHERE l.parentLocation.id = :parentId AND l.status = 'ACTIVE'")
    List<Location> findDirectChildren(@Param("parentId") Long parentId);
}