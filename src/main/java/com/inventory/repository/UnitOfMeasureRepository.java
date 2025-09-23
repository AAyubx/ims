package com.inventory.repository;

import com.inventory.entity.UnitOfMeasure;
import com.inventory.entity.UnitOfMeasure.UnitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {

    /**
     * Find all units of measure for a tenant with pagination
     */
    Page<UnitOfMeasure> findByTenant_Id(Long tenantId, Pageable pageable);

    /**
     * Find all units of measure for a tenant
     */
    List<UnitOfMeasure> findByTenant_Id(Long tenantId);

    /**
     * Find unit of measure by tenant and code
     */
    Optional<UnitOfMeasure> findByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Find unit of measure by tenant and id
     */
    Optional<UnitOfMeasure> findByTenant_IdAndId(Long tenantId, Long id);

    /**
     * Check if UoM code exists for tenant
     */
    boolean existsByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Check if UoM code exists for tenant excluding specific id
     */
    boolean existsByTenant_IdAndCodeAndIdNot(Long tenantId, String code, Long id);

    /**
     * Find units by type
     */
    List<UnitOfMeasure> findByTenant_IdAndUnitType(Long tenantId, UnitType unitType);

    /**
     * Find base units for a tenant
     */
    List<UnitOfMeasure> findByTenant_IdAndIsBaseUnit(Long tenantId, Boolean isBaseUnit);

    /**
     * Find base units by type
     */
    List<UnitOfMeasure> findByTenant_IdAndUnitTypeAndIsBaseUnit(Long tenantId, UnitType unitType, Boolean isBaseUnit);

    /**
     * Search units by name or code
     */
    @Query("SELECT u FROM UnitOfMeasure u WHERE u.tenant.id = :tenantId " +
           "AND (UPPER(u.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(u.code) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    Page<UnitOfMeasure> searchByNameOrCode(@Param("tenantId") Long tenantId, 
                                         @Param("searchTerm") String searchTerm, 
                                         Pageable pageable);

    /**
     * Find units by type with pagination
     */
    Page<UnitOfMeasure> findByTenant_IdAndUnitType(Long tenantId, UnitType unitType, Pageable pageable);

    /**
     * Count units by type
     */
    long countByTenant_IdAndUnitType(Long tenantId, UnitType unitType);

    /**
     * Find units commonly used for selling
     */
    @Query("SELECT DISTINCT u FROM UnitOfMeasure u WHERE u.tenant.id = :tenantId " +
           "AND u.code IN ('EA', 'BOX', 'CASE', 'KG', 'G', 'L', 'ML')")
    List<UnitOfMeasure> findCommonSellingUnits(@Param("tenantId") Long tenantId);

    /**
     * Find count-type units (EA, BOX, CASE, etc.)
     */
    List<UnitOfMeasure> findByTenant_IdAndUnitTypeOrderByCode(Long tenantId, UnitType unitType);

    /**
     * Find all unit types used by tenant
     */
    @Query("SELECT DISTINCT u.unitType FROM UnitOfMeasure u WHERE u.tenant.id = :tenantId")
    List<UnitType> findDistinctUnitTypesByTenant(@Param("tenantId") Long tenantId);
}