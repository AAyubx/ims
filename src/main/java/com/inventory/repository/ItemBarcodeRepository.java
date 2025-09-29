package com.inventory.repository;

import com.inventory.entity.ItemBarcode;
import com.inventory.entity.BarcodeStatus;
import com.inventory.entity.BarcodeType;
import com.inventory.entity.PackLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemBarcodeRepository extends JpaRepository<ItemBarcode, Long> {

    /**
     * Find barcode by code and tenant
     */
    Optional<ItemBarcode> findByTenantIdAndBarcode(Long tenantId, String barcode);

    /**
     * Find all barcodes for a variant
     */
    List<ItemBarcode> findByTenantIdAndVariantIdOrderByIsPrimaryDescCreatedAtAsc(Long tenantId, Long variantId);

    /**
     * Find primary barcode for a variant and pack level
     */
    Optional<ItemBarcode> findByTenantIdAndVariantIdAndPackLevelAndIsPrimaryTrue(Long tenantId, Long variantId, PackLevel packLevel);

    /**
     * Find barcodes by status
     */
    Page<ItemBarcode> findByTenantIdAndStatus(Long tenantId, BarcodeStatus status, Pageable pageable);

    /**
     * Find barcodes by type
     */
    List<ItemBarcode> findByTenantIdAndBarcodeType(Long tenantId, BarcodeType barcodeType);

    /**
     * Find scannable barcodes (ACTIVE status)
     */
    @Query("SELECT ib FROM ItemBarcode ib WHERE ib.tenantId = :tenantId AND ib.status = 'ACTIVE'")
    List<ItemBarcode> findScannableBarcodes(@Param("tenantId") Long tenantId);

    /**
     * Check if barcode exists for tenant (excluding specific ID)
     */
    @Query("SELECT COUNT(ib) > 0 FROM ItemBarcode ib WHERE ib.tenantId = :tenantId AND ib.barcode = :barcode AND (:id IS NULL OR ib.id != :id)")
    boolean existsByTenantIdAndBarcodeExcludingId(@Param("tenantId") Long tenantId, @Param("barcode") String barcode, @Param("id") Long id);

    /**
     * Find primary barcodes for multiple variants
     */
    @Query("SELECT ib FROM ItemBarcode ib WHERE ib.tenantId = :tenantId AND ib.variant.id IN :variantIds AND ib.isPrimary = true")
    List<ItemBarcode> findPrimaryBarcodesForVariants(@Param("tenantId") Long tenantId, @Param("variantIds") List<Long> variantIds);

    /**
     * Count barcodes by status for tenant
     */
    @Query("SELECT ib.status, COUNT(ib) FROM ItemBarcode ib WHERE ib.tenantId = :tenantId GROUP BY ib.status")
    List<Object[]> countByStatusForTenant(@Param("tenantId") Long tenantId);

    /**
     * Find barcodes requiring capacity attention (deprecated or low utilization)
     */
    @Query("SELECT ib FROM ItemBarcode ib WHERE ib.tenantId = :tenantId AND ib.status IN ('DEPRECATED', 'BLOCKED')")
    List<ItemBarcode> findBarcodesRequiringAttention(@Param("tenantId") Long tenantId);

    /**
     * Find barcodes by pack level
     */
    List<ItemBarcode> findByTenantIdAndPackLevelOrderByCreatedAtDesc(Long tenantId, PackLevel packLevel);

    /**
     * Search barcodes by partial barcode match
     */
    @Query("SELECT ib FROM ItemBarcode ib WHERE ib.tenantId = :tenantId AND ib.barcode LIKE %:barcode% AND ib.status = 'ACTIVE'")
    List<ItemBarcode> searchActiveBarcodesContaining(@Param("tenantId") Long tenantId, @Param("barcode") String barcode);
}