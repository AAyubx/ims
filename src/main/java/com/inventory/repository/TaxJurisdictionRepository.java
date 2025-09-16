package com.inventory.repository;

import com.inventory.entity.TaxJurisdiction;
import com.inventory.entity.TaxJurisdiction.TaxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaxJurisdictionRepository extends JpaRepository<TaxJurisdiction, Long> {

    /**
     * Find all tax jurisdictions for a tenant
     */
    Page<TaxJurisdiction> findByTenant_Id(Long tenantId, Pageable pageable);

    /**
     * Find tax jurisdiction by tenant and code
     */
    Optional<TaxJurisdiction> findByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Find tax jurisdictions by country
     */
    List<TaxJurisdiction> findByTenant_IdAndCountryCode(Long tenantId, String countryCode);

    /**
     * Find tax jurisdictions by country and state/province
     */
    List<TaxJurisdiction> findByTenant_IdAndCountryCodeAndStateProvince(Long tenantId, String countryCode, String stateProvince);

    /**
     * Find tax jurisdictions by type
     */
    List<TaxJurisdiction> findByTenant_IdAndTaxType(Long tenantId, TaxType taxType);

    /**
     * Find effective tax jurisdictions (current date falls within effective/expiry range)
     */
    @Query("SELECT tj FROM TaxJurisdiction tj WHERE tj.tenant.id = :tenantId " +
           "AND tj.effectiveDate <= :currentDate " +
           "AND (tj.expiryDate IS NULL OR tj.expiryDate > :currentDate)")
    List<TaxJurisdiction> findEffectiveJurisdictions(@Param("tenantId") Long tenantId,
                                                    @Param("currentDate") LocalDate currentDate);

    /**
     * Find effective tax jurisdiction for a specific location (country + state/province)
     */
    @Query("SELECT tj FROM TaxJurisdiction tj WHERE tj.tenant.id = :tenantId " +
           "AND tj.countryCode = :countryCode " +
           "AND (tj.stateProvince IS NULL OR tj.stateProvince = :stateProvince) " +
           "AND tj.effectiveDate <= :currentDate " +
           "AND (tj.expiryDate IS NULL OR tj.expiryDate > :currentDate) " +
           "ORDER BY tj.stateProvince DESC, tj.effectiveDate DESC")
    List<TaxJurisdiction> findApplicableJurisdictions(@Param("tenantId") Long tenantId,
                                                     @Param("countryCode") String countryCode,
                                                     @Param("stateProvince") String stateProvince,
                                                     @Param("currentDate") LocalDate currentDate);

    /**
     * Check if tax jurisdiction code exists for tenant
     */
    boolean existsByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Find jurisdictions expiring soon
     */
    @Query("SELECT tj FROM TaxJurisdiction tj WHERE tj.tenant.id = :tenantId " +
           "AND tj.expiryDate IS NOT NULL " +
           "AND tj.expiryDate BETWEEN :startDate AND :endDate")
    List<TaxJurisdiction> findJurisdictionsExpiringSoon(@Param("tenantId") Long tenantId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}