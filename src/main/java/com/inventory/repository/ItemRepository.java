package com.inventory.repository;

import com.inventory.entity.Item;
import com.inventory.entity.Item.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find all items for a tenant with pagination
     */
    Page<Item> findByTenant_Id(Long tenantId, Pageable pageable);

    /**
     * Find all active items for a tenant
     */
    List<Item> findByTenant_IdAndStatus(Long tenantId, ItemStatus status);

    /**
     * Find item by tenant and SKU
     */
    Optional<Item> findByTenant_IdAndSku(Long tenantId, String sku);

    /**
     * Find item by tenant and id
     */
    Optional<Item> findByTenant_IdAndId(Long tenantId, Long id);

    /**
     * Check if SKU exists for tenant
     */
    boolean existsByTenant_IdAndSku(Long tenantId, String sku);

    /**
     * Check if SKU exists for tenant excluding specific id
     */
    boolean existsByTenant_IdAndSkuAndIdNot(Long tenantId, String sku, Long id);

    /**
     * Find items by category
     */
    Page<Item> findByTenant_IdAndCategory_Id(Long tenantId, Long categoryId, Pageable pageable);

    /**
     * Find items by department
     */
    Page<Item> findByTenant_IdAndDepartment_Id(Long tenantId, Long departmentId, Pageable pageable);

    /**
     * Find items by brand
     */
    Page<Item> findByTenant_IdAndBrand_Id(Long tenantId, Long brandId, Pageable pageable);

    /**
     * Find items by status
     */
    Page<Item> findByTenant_IdAndStatus(Long tenantId, ItemStatus status, Pageable pageable);

    /**
     * Search items by name, SKU, or description
     */
    @Query("SELECT i FROM Item i WHERE i.tenant.id = :tenantId " +
           "AND (UPPER(i.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(i.sku) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(i.description) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    Page<Item> searchByNameSkuOrDescription(@Param("tenantId") Long tenantId, 
                                          @Param("searchTerm") String searchTerm, 
                                          Pageable pageable);

    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT i FROM Item i WHERE i.tenant.id = :tenantId " +
           "AND (:departmentId IS NULL OR i.department.id = :departmentId) " +
           "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
           "AND (:brandId IS NULL OR i.brand.id = :brandId) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:searchTerm IS NULL OR " +
           "     UPPER(i.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "     UPPER(i.sku) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "     UPPER(i.description) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    Page<Item> advancedSearch(@Param("tenantId") Long tenantId,
                            @Param("departmentId") Long departmentId,
                            @Param("categoryId") Long categoryId,
                            @Param("brandId") Long brandId,
                            @Param("status") ItemStatus status,
                            @Param("searchTerm") String searchTerm,
                            Pageable pageable);

    /**
     * Count items by status for a tenant
     */
    long countByTenant_IdAndStatus(Long tenantId, ItemStatus status);

    /**
     * Find items with variant count
     */
    @Query("SELECT i, COUNT(v) FROM Item i LEFT JOIN i.variants v " +
           "WHERE i.tenant.id = :tenantId AND i.status = :status " +
           "GROUP BY i ORDER BY i.name")
    List<Object[]> findItemsWithVariantCount(@Param("tenantId") Long tenantId, 
                                           @Param("status") ItemStatus status);

    /**
     * Find parent items (items that have variants)
     */
    @Query("SELECT DISTINCT i FROM Item i INNER JOIN i.variants v " +
           "WHERE i.tenant.id = :tenantId AND i.status = :status")
    List<Item> findParentItems(@Param("tenantId") Long tenantId, @Param("status") ItemStatus status);

    /**
     * Find items by supplier
     * TODO: Uncomment when SupplierItem entity and relationships are implemented
     */
    // @Query("SELECT DISTINCT i FROM Item i INNER JOIN i.variants v INNER JOIN v.supplierItems si " +
    //        "WHERE i.tenant.id = :tenantId AND si.supplier.id = :supplierId")
    // List<Item> findBySupplier(@Param("tenantId") Long tenantId, @Param("supplierId") Long supplierId);

    /**
     * Find items needing supplier setup
     * TODO: Uncomment when SupplierItem entity and relationships are implemented
     */
    // @Query("SELECT i FROM Item i WHERE i.tenant.id = :tenantId " +
    //        "AND i.status = 'ACTIVE' " +
    //        "AND NOT EXISTS (SELECT 1 FROM ItemVariant v JOIN v.supplierItems si WHERE v.item = i)")
    // List<Item> findItemsNeedingSupplierSetup(@Param("tenantId") Long tenantId);

    /**
     * Find items by attribute value
     */
    @Query("SELECT DISTINCT i FROM Item i INNER JOIN i.attributeValues av " +
           "WHERE i.tenant.id = :tenantId " +
           "AND av.attributeDefinition.id = :attributeId " +
           "AND UPPER(av.value) LIKE UPPER(CONCAT('%', :value, '%'))")
    List<Item> findByAttributeValue(@Param("tenantId") Long tenantId,
                                  @Param("attributeId") Long attributeId,
                                  @Param("value") String value);
}