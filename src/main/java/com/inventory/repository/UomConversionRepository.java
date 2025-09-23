package com.inventory.repository;

import com.inventory.entity.UomConversion;
import com.inventory.entity.UnitOfMeasure.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UomConversionRepository extends JpaRepository<UomConversion, Long> {

    /**
     * Find conversion between two specific units
     */
    Optional<UomConversion> findByFromUom_IdAndToUom_Id(Long fromUomId, Long toUomId);

    /**
     * Find all conversions from a specific unit
     */
    List<UomConversion> findByFromUom_Id(Long fromUomId);

    /**
     * Find all conversions to a specific unit
     */
    List<UomConversion> findByToUom_Id(Long toUomId);

    /**
     * Find all conversions for a tenant
     */
    List<UomConversion> findByTenant_Id(Long tenantId);

    /**
     * Find conversions between units of the same type
     */
    @Query("SELECT c FROM UomConversion c " +
           "WHERE c.tenant.id = :tenantId " +
           "AND c.fromUom.unitType = c.toUom.unitType " +
           "AND c.fromUom.unitType = :unitType")
    List<UomConversion> findConversionsByUnitType(@Param("tenantId") Long tenantId, 
                                                @Param("unitType") UnitType unitType);

    /**
     * Find conversion factor between two units by codes
     */
    @Query("SELECT c.conversionFactor FROM UomConversion c " +
           "WHERE c.tenant.id = :tenantId " +
           "AND c.fromUom.code = :fromCode " +
           "AND c.toUom.code = :toCode")
    Optional<BigDecimal> findConversionFactor(@Param("tenantId") Long tenantId,
                                            @Param("fromCode") String fromCode,
                                            @Param("toCode") String toCode);

    /**
     * Check if conversion exists between two units
     */
    boolean existsByFromUom_IdAndToUom_Id(Long fromUomId, Long toUomId);

    /**
     * Find all conversions involving a specific unit (either from or to)
     */
    @Query("SELECT c FROM UomConversion c " +
           "WHERE c.fromUom.id = :uomId OR c.toUom.id = :uomId")
    List<UomConversion> findConversionsInvolvingUnit(@Param("uomId") Long uomId);

    /**
     * Find conversion path between two units (direct conversion)
     */
    @Query("SELECT c FROM UomConversion c " +
           "WHERE c.tenant.id = :tenantId " +
           "AND ((c.fromUom.id = :fromUomId AND c.toUom.id = :toUomId) " +
           "OR (c.fromUom.id = :toUomId AND c.toUom.id = :fromUomId))")
    List<UomConversion> findConversionPath(@Param("tenantId") Long tenantId,
                                         @Param("fromUomId") Long fromUomId,
                                         @Param("toUomId") Long toUomId);

    /**
     * Find conversions by tenant and unit type
     */
    @Query("SELECT c FROM UomConversion c JOIN c.fromUom f JOIN c.toUom t " +
           "WHERE c.tenant.id = :tenantId " +
           "AND f.unitType = :unitType AND t.unitType = :unitType")
    List<UomConversion> findByTenantAndUnitType(@Param("tenantId") Long tenantId, 
                                              @Param("unitType") UnitType unitType);

    /**
     * Count conversions for a tenant
     */
    long countByTenant_Id(Long tenantId);

    /**
     * Delete conversions involving a specific unit
     */
    @Query("DELETE FROM UomConversion c WHERE c.fromUom.id = :uomId OR c.toUom.id = :uomId")
    void deleteConversionsInvolvingUnit(@Param("uomId") Long uomId);
}