package com.inventory.repository;

import com.inventory.entity.ItemVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

    /**
     * Find variant by ID and tenant
     */
    Optional<ItemVariant> findByIdAndTenant_Id(Long id, Long tenantId);

    /**
     * Find all variants for a tenant with pagination
     */
    Page<ItemVariant> findByTenant_IdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    /**
     * Find variants by item ID
     * TODO: Add isDefault field to ItemVariant entity and implement this method
     */
    List<ItemVariant> findByTenant_IdAndItem_IdOrderByCreatedAtAsc(Long tenantId, Long itemId);

    /**
     * Find default variant for an item
     * TODO: Add isDefault field to ItemVariant entity and implement this method
     */
    // Optional<ItemVariant> findByTenant_IdAndItem_IdAndIsDefaultTrue(Long tenantId, Long itemId);

    /**
     * Check if variant SKU exists (excluding specific ID)
     */
    @Query("SELECT COUNT(v) > 0 FROM ItemVariant v WHERE v.tenant.id = :tenantId AND v.variantSku = :sku AND (:id IS NULL OR v.id != :id)")
    boolean existsByTenantIdAndSkuExcludingId(@Param("tenantId") Long tenantId, @Param("sku") String sku, @Param("id") Long id);

    /**
     * Find variants by SKU pattern
     */
    @Query("SELECT v FROM ItemVariant v WHERE v.tenant.id = :tenantId AND v.variantSku LIKE CONCAT('%',:sku,'%')")
    List<ItemVariant> findBySkuContaining(@Param("tenantId") Long tenantId, @Param("sku") String sku);

    /**
     * Count variants for an item
     */
    long countByTenant_IdAndItem_Id(Long tenantId, Long itemId);
}