package com.inventory.repository;

import com.inventory.entity.AttributeSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeSetRepository extends JpaRepository<AttributeSet, Long> {

    /**
     * Find all attribute sets for a category
     */
    List<AttributeSet> findByCategory_IdOrderByDisplayOrder(Long categoryId);

    /**
     * Find all attribute sets for a tenant
     */
    List<AttributeSet> findByTenant_Id(Long tenantId);

    /**
     * Find attribute set by category and attribute definition
     */
    Optional<AttributeSet> findByCategory_IdAndAttributeDefinition_Id(Long categoryId, Long attributeDefinitionId);

    /**
     * Find required attributes for a category
     */
    List<AttributeSet> findByCategory_IdAndIsRequiredForVariants(Long categoryId, Boolean isRequired);

    /**
     * Find attribute sets for a specific attribute definition
     */
    List<AttributeSet> findByAttributeDefinition_Id(Long attributeDefinitionId);

    /**
     * Find attribute sets by tenant and category
     */
    List<AttributeSet> findByTenant_IdAndCategory_Id(Long tenantId, Long categoryId);

    /**
     * Check if category has specific attribute
     */
    boolean existsByCategory_IdAndAttributeDefinition_Id(Long categoryId, Long attributeDefinitionId);

    /**
     * Find categories using a specific attribute
     */
    @Query("SELECT s FROM AttributeSet s JOIN FETCH s.category " +
           "WHERE s.attributeDefinition.id = :attributeDefinitionId")
    List<AttributeSet> findCategoriesUsingAttribute(@Param("attributeDefinitionId") Long attributeDefinitionId);

    /**
     * Find attribute sets with attribute details for a category
     */
    @Query("SELECT s FROM AttributeSet s JOIN FETCH s.attributeDefinition " +
           "WHERE s.category.id = :categoryId ORDER BY s.displayOrder")
    List<AttributeSet> findByCategoryWithAttributeDetails(@Param("categoryId") Long categoryId);

    /**
     * Count attributes for a category
     */
    long countByCategory_Id(Long categoryId);

    /**
     * Find required attributes for variant creation
     */
    @Query("SELECT s FROM AttributeSet s JOIN FETCH s.attributeDefinition " +
           "WHERE s.category.id = :categoryId AND s.isRequiredForVariants = true " +
           "ORDER BY s.displayOrder")
    List<AttributeSet> findRequiredAttributesForCategory(@Param("categoryId") Long categoryId);

    /**
     * Find attribute sets by tenant and attribute definition
     */
    List<AttributeSet> findByTenant_IdAndAttributeDefinition_Id(Long tenantId, Long attributeDefinitionId);

    /**
     * Get next display order for category
     */
    @Query("SELECT COALESCE(MAX(s.displayOrder), 0) + 1 FROM AttributeSet s WHERE s.category.id = :categoryId")
    Integer getNextDisplayOrder(@Param("categoryId") Long categoryId);

    /**
     * Delete attribute set by category and attribute definition
     */
    void deleteByCategory_IdAndAttributeDefinition_Id(Long categoryId, Long attributeDefinitionId);
}