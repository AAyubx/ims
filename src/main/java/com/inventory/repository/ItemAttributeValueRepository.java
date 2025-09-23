package com.inventory.repository;

import com.inventory.entity.ItemAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemAttributeValueRepository extends JpaRepository<ItemAttributeValue, Long> {

    /**
     * Find all attribute values for an item
     */
    List<ItemAttributeValue> findByItem_Id(Long itemId);

    /**
     * Find attribute value by item and attribute definition
     */
    Optional<ItemAttributeValue> findByItem_IdAndAttributeDefinition_Id(Long itemId, Long attributeDefinitionId);

    /**
     * Find all attribute values for a tenant
     */
    List<ItemAttributeValue> findByTenant_Id(Long tenantId);

    /**
     * Find items with specific attribute value
     */
    List<ItemAttributeValue> findByAttributeDefinition_IdAndValue(Long attributeDefinitionId, String value);

    /**
     * Find attribute values by tenant and attribute definition
     */
    List<ItemAttributeValue> findByTenant_IdAndAttributeDefinition_Id(Long tenantId, Long attributeDefinitionId);

    /**
     * Find attribute values with item and attribute details
     */
    @Query("SELECT v FROM ItemAttributeValue v " +
           "JOIN FETCH v.item " +
           "JOIN FETCH v.attributeDefinition " +
           "WHERE v.item.id = :itemId")
    List<ItemAttributeValue> findByItemWithDetails(@Param("itemId") Long itemId);

    /**
     * Check if item has specific attribute value
     */
    boolean existsByItem_IdAndAttributeDefinition_Id(Long itemId, Long attributeDefinitionId);

    /**
     * Find distinct attribute values for a specific attribute
     */
    @Query("SELECT DISTINCT v.value FROM ItemAttributeValue v " +
           "WHERE v.tenant.id = :tenantId " +
           "AND v.attributeDefinition.id = :attributeDefinitionId " +
           "ORDER BY v.value")
    List<String> findDistinctValuesByAttribute(@Param("tenantId") Long tenantId, 
                                             @Param("attributeDefinitionId") Long attributeDefinitionId);

    /**
     * Count items by attribute value
     */
    @Query("SELECT v.value, COUNT(v) FROM ItemAttributeValue v " +
           "WHERE v.tenant.id = :tenantId " +
           "AND v.attributeDefinition.id = :attributeDefinitionId " +
           "GROUP BY v.value")
    List<Object[]> countItemsByAttributeValue(@Param("tenantId") Long tenantId, 
                                            @Param("attributeDefinitionId") Long attributeDefinitionId);

    /**
     * Find items with multiple attribute values
     */
    @Query("SELECT v.item.id FROM ItemAttributeValue v " +
           "WHERE v.attributeDefinition.id IN :attributeDefinitionIds " +
           "AND v.value IN :values " +
           "GROUP BY v.item.id " +
           "HAVING COUNT(DISTINCT v.attributeDefinition.id) = :attributeCount")
    List<Long> findItemsWithAttributeValues(@Param("attributeDefinitionIds") List<Long> attributeDefinitionIds,
                                          @Param("values") List<String> values,
                                          @Param("attributeCount") long attributeCount);

    /**
     * Delete all attribute values for an item
     */
    void deleteByItem_Id(Long itemId);

    /**
     * Delete attribute value by item and attribute definition
     */
    void deleteByItem_IdAndAttributeDefinition_Id(Long itemId, Long attributeDefinitionId);

    /**
     * Find attribute values by tenant and attribute code
     */
    @Query("SELECT v FROM ItemAttributeValue v " +
           "WHERE v.tenant.id = :tenantId " +
           "AND v.attributeDefinition.code = :attributeCode")
    List<ItemAttributeValue> findByTenantAndAttributeCode(@Param("tenantId") Long tenantId, 
                                                         @Param("attributeCode") String attributeCode);
}