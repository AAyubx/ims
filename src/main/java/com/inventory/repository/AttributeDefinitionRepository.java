package com.inventory.repository;

import com.inventory.entity.AttributeDefinition;
import com.inventory.entity.AttributeDefinition.AttributeDataType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {

    /**
     * Find all attribute definitions for a tenant with pagination
     */
    Page<AttributeDefinition> findByTenant_Id(Long tenantId, Pageable pageable);

    /**
     * Find all attribute definitions for a tenant
     */
    List<AttributeDefinition> findByTenant_Id(Long tenantId);

    /**
     * Find attribute definition by tenant and code
     */
    Optional<AttributeDefinition> findByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Find attribute definition by tenant and id
     */
    Optional<AttributeDefinition> findByTenant_IdAndId(Long tenantId, Long id);

    /**
     * Check if attribute code exists for tenant
     */
    boolean existsByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Check if attribute code exists for tenant excluding specific id
     */
    boolean existsByTenant_IdAndCodeAndIdNot(Long tenantId, String code, Long id);

    /**
     * Find attributes by data type
     */
    List<AttributeDefinition> findByTenant_IdAndDataType(Long tenantId, AttributeDataType dataType);

    /**
     * Find required attributes
     */
    List<AttributeDefinition> findByTenant_IdAndIsRequired(Long tenantId, Boolean isRequired);

    /**
     * Search attributes by name or code
     */
    @Query("SELECT a FROM AttributeDefinition a WHERE a.tenant.id = :tenantId " +
           "AND (UPPER(a.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(a.code) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    Page<AttributeDefinition> searchByNameOrCode(@Param("tenantId") Long tenantId, 
                                                @Param("searchTerm") String searchTerm, 
                                                Pageable pageable);

    /**
     * Find attributes by data type with pagination
     */
    Page<AttributeDefinition> findByTenant_IdAndDataType(Long tenantId, AttributeDataType dataType, Pageable pageable);

    /**
     * Count attributes by data type
     */
    long countByTenant_IdAndDataType(Long tenantId, AttributeDataType dataType);

    /**
     * Find attributes used in attribute sets (categories)
     */
    @Query("SELECT DISTINCT a FROM AttributeDefinition a JOIN a.attributeSets s " +
           "WHERE a.tenant.id = :tenantId")
    List<AttributeDefinition> findAttributesUsedInCategories(@Param("tenantId") Long tenantId);

    /**
     * Find unused attributes (not assigned to any category)
     */
    @Query("SELECT a FROM AttributeDefinition a WHERE a.tenant.id = :tenantId " +
           "AND NOT EXISTS (SELECT 1 FROM AttributeSet s WHERE s.attributeDefinition = a)")
    List<AttributeDefinition> findUnusedAttributes(@Param("tenantId") Long tenantId);

    /**
     * Find attributes with usage count
     */
    @Query("SELECT a, COUNT(s) FROM AttributeDefinition a LEFT JOIN a.attributeSets s " +
           "WHERE a.tenant.id = :tenantId " +
           "GROUP BY a ORDER BY a.name")
    List<Object[]> findAttributesWithUsageCount(@Param("tenantId") Long tenantId);

    /**
     * Find list-type attributes for dropdown options
     */
    @Query("SELECT a FROM AttributeDefinition a WHERE a.tenant.id = :tenantId " +
           "AND a.dataType = 'LIST' AND a.allowedValues IS NOT NULL")
    List<AttributeDefinition> findListTypeAttributes(@Param("tenantId") Long tenantId);
}