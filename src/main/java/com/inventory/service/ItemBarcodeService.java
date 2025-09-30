package com.inventory.service;

import com.inventory.entity.*;
import com.inventory.repository.ItemBarcodeRepository;
import com.inventory.repository.ItemVariantRepository;
import com.inventory.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing item barcodes including CRUD operations and business logic.
 * Handles barcode assignment, validation, status management, and primary barcode logic.
 */
@Service
@Transactional
public class ItemBarcodeService {

    @Autowired
    private ItemBarcodeRepository itemBarcodeRepository;

    @Autowired
    private ItemVariantRepository itemVariantRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private BarcodeValidationService barcodeValidationService;

    @Autowired
    private BarcodeGeneratorService barcodeGeneratorService;

    /**
     * Create a new barcode for an item variant
     */
    public ItemBarcode createBarcode(Long tenantId, Long variantId, String barcode, BarcodeType barcodeType, 
                                   PackLevel packLevel, UnitOfMeasure unitOfMeasure, Long userId) {
        
        // Validate inputs
        ItemVariant variant = itemVariantRepository.findByIdAndTenant_Id(variantId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Item variant not found"));
        
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Validate barcode format
        BarcodeValidationService.ValidationResult validation = 
                barcodeValidationService.validateBarcode(barcode, barcodeType, packLevel != null ? packLevel : PackLevel.EACH);
        
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid barcode: " + validation.getErrorMessage());
        }

        // Check for duplicate barcode
        if (itemBarcodeRepository.existsByTenantIdAndBarcodeExcludingId(tenantId, barcode, null)) {
            throw new IllegalArgumentException("Barcode already exists: " + barcode);
        }

        // Create barcode entity
        ItemBarcode itemBarcode = new ItemBarcode(variant, barcode, barcodeType, user);
        itemBarcode.setPackLevel(packLevel != null ? packLevel : PackLevel.EACH);
        itemBarcode.setUnitOfMeasure(unitOfMeasure);
        itemBarcode.setStatus(BarcodeStatus.RESERVED);

        return itemBarcodeRepository.save(itemBarcode);
    }

    /**
     * Generate and create a new barcode for an item variant
     */
    public ItemBarcode generateAndCreateBarcode(Long tenantId, Long variantId, BarcodeType barcodeType, 
                                              PackLevel packLevel, UnitOfMeasure unitOfMeasure, Long userId) {
        
        // Generate barcode
        BarcodeGeneratorService.GenerationResult result = 
                barcodeGeneratorService.generateBarcode(tenantId, barcodeType, packLevel != null ? packLevel : PackLevel.EACH);
        
        if (!result.isSuccess()) {
            throw new IllegalStateException("Failed to generate barcode: " + result.getErrorMessage());
        }

        return createBarcode(tenantId, variantId, result.getBarcode(), barcodeType, packLevel, unitOfMeasure, userId);
    }

    /**
     * Update barcode details
     */
    public ItemBarcode updateBarcode(Long tenantId, Long barcodeId, String newBarcode, BarcodeType barcodeType, 
                                   PackLevel packLevel, UnitOfMeasure unitOfMeasure, Long userId) {
        
        ItemBarcode barcode = getBarcode(tenantId, barcodeId);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check if barcode can be modified
        if (!barcode.getStatus().isModifiable()) {
            throw new IllegalStateException("Barcode cannot be modified in " + barcode.getStatus() + " status");
        }

        // Validate new barcode if changed
        if (newBarcode != null && !newBarcode.equals(barcode.getBarcode())) {
            BarcodeValidationService.ValidationResult validation = 
                    barcodeValidationService.validateBarcode(newBarcode, barcodeType, packLevel != null ? packLevel : PackLevel.EACH);
            
            if (!validation.isValid()) {
                throw new IllegalArgumentException("Invalid barcode: " + validation.getErrorMessage());
            }

            // Check for duplicate
            if (itemBarcodeRepository.existsByTenantIdAndBarcodeExcludingId(tenantId, newBarcode, barcodeId)) {
                throw new IllegalArgumentException("Barcode already exists: " + newBarcode);
            }

            barcode.setBarcode(newBarcode);
        }

        // Update fields
        if (barcodeType != null) {
            barcode.setBarcodeType(barcodeType);
        }
        if (packLevel != null) {
            barcode.setPackLevel(packLevel);
        }
        if (unitOfMeasure != null) {
            barcode.setUnitOfMeasure(unitOfMeasure);
        }

        barcode.setUpdatedAt(LocalDateTime.now());

        return itemBarcodeRepository.save(barcode);
    }

    /**
     * Set barcode as primary for its variant/pack level combination
     */
    @Transactional
    public ItemBarcode setPrimary(Long tenantId, Long barcodeId, Long userId) {
        ItemBarcode barcode = getBarcode(tenantId, barcodeId);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!barcode.canBePrimary()) {
            throw new IllegalStateException("Barcode cannot be set as primary in current state");
        }

        PackLevel effectivePackLevel = barcode.getEffectivePackLevel();

        // Remove primary flag from other barcodes for this variant/pack level
        Optional<ItemBarcode> currentPrimary = itemBarcodeRepository
                .findByTenantIdAndVariantIdAndPackLevelAndIsPrimaryTrue(tenantId, barcode.getVariant().getId(), effectivePackLevel);
        
        if (currentPrimary.isPresent() && !currentPrimary.get().getId().equals(barcodeId)) {
            ItemBarcode current = currentPrimary.get();
            current.setIsPrimary(false);
            current.setUpdatedAt(LocalDateTime.now());
            itemBarcodeRepository.save(current);
        }

        // Set this barcode as primary
        barcode.setIsPrimary(true);
        barcode.setUpdatedAt(LocalDateTime.now());

        return itemBarcodeRepository.save(barcode);
    }

    /**
     * Activate barcode (change status from RESERVED to ACTIVE)
     */
    public ItemBarcode activateBarcode(Long tenantId, Long barcodeId, Long userId) {
        ItemBarcode barcode = getBarcode(tenantId, barcodeId);
        
        if (!barcode.getStatus().canTransitionTo(BarcodeStatus.ACTIVE)) {
            throw new IllegalStateException("Cannot activate barcode from " + barcode.getStatus() + " status");
        }

        return changeStatus(tenantId, barcodeId, BarcodeStatus.ACTIVE, userId);
    }

    /**
     * Deprecate barcode
     */
    public ItemBarcode deprecateBarcode(Long tenantId, Long barcodeId, Long userId) {
        ItemBarcode barcode = getBarcode(tenantId, barcodeId);
        
        if (!barcode.getStatus().canTransitionTo(BarcodeStatus.DEPRECATED)) {
            throw new IllegalStateException("Cannot deprecate barcode from " + barcode.getStatus() + " status");
        }

        // Remove primary flag if set
        if (barcode.getIsPrimary()) {
            barcode.setIsPrimary(false);
        }

        return changeStatus(tenantId, barcodeId, BarcodeStatus.DEPRECATED, userId);
    }

    /**
     * Block barcode
     */
    public ItemBarcode blockBarcode(Long tenantId, Long barcodeId, Long userId) {
        ItemBarcode barcode = getBarcode(tenantId, barcodeId);
        
        if (!barcode.getStatus().canTransitionTo(BarcodeStatus.BLOCKED)) {
            throw new IllegalStateException("Cannot block barcode from " + barcode.getStatus() + " status");
        }

        // Remove primary flag if set
        if (barcode.getIsPrimary()) {
            barcode.setIsPrimary(false);
        }

        return changeStatus(tenantId, barcodeId, BarcodeStatus.BLOCKED, userId);
    }

    /**
     * Change barcode status
     */
    private ItemBarcode changeStatus(Long tenantId, Long barcodeId, BarcodeStatus newStatus, Long userId) {
        ItemBarcode barcode = getBarcode(tenantId, barcodeId);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        barcode.setStatus(newStatus);
        barcode.setUpdatedAt(LocalDateTime.now());

        return itemBarcodeRepository.save(barcode);
    }

    /**
     * Delete barcode (only if deletable)
     */
    public void deleteBarcode(Long tenantId, Long barcodeId) {
        ItemBarcode barcode = getBarcode(tenantId, barcodeId);
        
        if (!barcode.getStatus().isDeletable()) {
            throw new IllegalStateException("Cannot delete barcode in " + barcode.getStatus() + " status");
        }

        itemBarcodeRepository.delete(barcode);
    }

    /**
     * Get barcode by ID
     */
    public ItemBarcode getBarcode(Long tenantId, Long barcodeId) {
        return itemBarcodeRepository.findById(barcodeId)
                .filter(barcode -> barcode.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Barcode not found"));
    }

    /**
     * Find barcode by code
     */
    public Optional<ItemBarcode> findByBarcode(Long tenantId, String barcode) {
        return itemBarcodeRepository.findByTenantIdAndBarcode(tenantId, barcode);
    }

    /**
     * Get all barcodes for a variant
     */
    public List<ItemBarcode> getVariantBarcodes(Long tenantId, Long variantId) {
        return itemBarcodeRepository.findByTenantIdAndVariantIdOrderByIsPrimaryDescCreatedAtAsc(tenantId, variantId);
    }

    /**
     * Get primary barcode for variant and pack level
     */
    public Optional<ItemBarcode> getPrimaryBarcode(Long tenantId, Long variantId, PackLevel packLevel) {
        return itemBarcodeRepository.findByTenantIdAndVariantIdAndPackLevelAndIsPrimaryTrue(tenantId, variantId, packLevel);
    }

    /**
     * Get barcodes by status with pagination
     */
    public Page<ItemBarcode> getBarcodesByStatus(Long tenantId, BarcodeStatus status, Pageable pageable) {
        return itemBarcodeRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    /**
     * Search active barcodes
     */
    public List<ItemBarcode> searchActiveBarcodes(Long tenantId, String searchTerm) {
        return itemBarcodeRepository.searchActiveBarcodesContaining(tenantId, searchTerm);
    }

    /**
     * Get scannable barcodes (ACTIVE status)
     */
    public List<ItemBarcode> getScannableBarcodes(Long tenantId) {
        return itemBarcodeRepository.findScannableBarcodes(tenantId);
    }

    /**
     * Get barcode statistics for tenant
     */
    public BarcodeStatistics getBarcodeStatistics(Long tenantId) {
        List<Object[]> statusCounts = itemBarcodeRepository.countByStatusForTenant(tenantId);
        
        BarcodeStatistics stats = new BarcodeStatistics();
        for (Object[] row : statusCounts) {
            BarcodeStatus status = (BarcodeStatus) row[0];
            Long count = (Long) row[1];
            
            switch (status) {
                case RESERVED:
                    stats.setReservedCount(count);
                    break;
                case ACTIVE:
                    stats.setActiveCount(count);
                    break;
                case DEPRECATED:
                    stats.setDeprecatedCount(count);
                    break;
                case BLOCKED:
                    stats.setBlockedCount(count);
                    break;
            }
        }
        
        return stats;
    }

    /**
     * Get barcodes requiring attention
     */
    public List<ItemBarcode> getBarcodesRequiringAttention(Long tenantId) {
        return itemBarcodeRepository.findBarcodesRequiringAttention(tenantId);
    }

    /**
     * Statistics class for barcode counts
     */
    public static class BarcodeStatistics {
        private Long reservedCount = 0L;
        private Long activeCount = 0L;
        private Long deprecatedCount = 0L;
        private Long blockedCount = 0L;

        // Getters and setters
        public Long getReservedCount() { return reservedCount; }
        public void setReservedCount(Long reservedCount) { this.reservedCount = reservedCount; }
        
        public Long getActiveCount() { return activeCount; }
        public void setActiveCount(Long activeCount) { this.activeCount = activeCount; }
        
        public Long getDeprecatedCount() { return deprecatedCount; }
        public void setDeprecatedCount(Long deprecatedCount) { this.deprecatedCount = deprecatedCount; }
        
        public Long getBlockedCount() { return blockedCount; }
        public void setBlockedCount(Long blockedCount) { this.blockedCount = blockedCount; }
        
        public Long getTotalCount() {
            return reservedCount + activeCount + deprecatedCount + blockedCount;
        }
    }
}