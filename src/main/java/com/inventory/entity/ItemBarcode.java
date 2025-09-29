package com.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing a barcode associated with an item variant.
 * Supports multiple barcode types, UoM-specific codes, and pack levels.
 */
@Entity
@Table(name = "item_barcode", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_barcode_tenant", columnNames = {"tenant_id", "barcode"})
       },
       indexes = {
           @Index(name = "idx_barcode_variant", columnList = "tenant_id, variant_id"),
           @Index(name = "idx_barcode_primary", columnList = "tenant_id, is_primary"),
           @Index(name = "idx_barcode_lookup", columnList = "tenant_id, barcode"),
           @Index(name = "idx_barcode_status", columnList = "tenant_id, status"),
           @Index(name = "idx_barcode_type", columnList = "tenant_id, barcode_type"),
           @Index(name = "idx_barcode_pack_level", columnList = "tenant_id, pack_level")
       })
public class ItemBarcode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    @NotNull
    private Long tenantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    @NotNull
    @JsonIgnore
    private ItemVariant variant;
    
    @Column(name = "barcode", nullable = false, length = 64)
    @NotNull
    @Size(min = 4, max = 64)
    private String barcode;
    
    @Column(name = "barcode_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    @NotNull
    private BarcodeType barcodeType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uom_id")
    @JsonIgnore
    private UnitOfMeasure unitOfMeasure;
    
    @Column(name = "pack_level", length = 16)
    @Enumerated(EnumType.STRING)
    private PackLevel packLevel;
    
    @Column(name = "is_primary", nullable = false)
    @NotNull
    private Boolean isPrimary = false;
    
    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @NotNull
    private BarcodeStatus status = BarcodeStatus.RESERVED;
    
    @Column(name = "ai_payload", columnDefinition = "JSON")
    @Type(type = "json")
    private Map<String, Object> aiPayload;
    
    @Column(name = "label_template_id")
    private Long labelTemplateId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @NotNull
    @JsonIgnore
    private UserAccount createdBy;
    
    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;
    
    // Constructors
    public ItemBarcode() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ItemBarcode(ItemVariant variant, String barcode, BarcodeType barcodeType, UserAccount createdBy) {
        this();
        this.variant = variant;
        this.tenantId = variant.getTenant().getId();
        this.barcode = barcode;
        this.barcodeType = barcodeType;
        this.createdBy = createdBy;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    
    /**
     * Check if this barcode can be set as primary for its variant/UoM/pack combination.
     */
    public boolean canBePrimary() {
        return status.isModifiable() && barcodeType.supportsPackLevel(packLevel);
    }
    
    /**
     * Get the effective pack level (defaults to EACH if null).
     */
    public PackLevel getEffectivePackLevel() {
        return packLevel != null ? packLevel : PackLevel.EACH;
    }
    
    /**
     * Check if this barcode requires UoM conversion.
     */
    public boolean requiresUomConversion() {
        PackLevel effectiveLevel = getEffectivePackLevel();
        return effectiveLevel.requiresUomConversion() && unitOfMeasure != null;
    }
    
    /**
     * Get display text for barcode type and format.
     */
    public String getDisplayFormat() {
        return String.format("%s (%s)", barcodeType.getDisplayName(), barcode);
    }
    
    /**
     * Check if barcode format is valid for its type.
     */
    public boolean isValidFormat() {
        if (barcodeType.hasFixedLength()) {
            return barcode.length() == barcodeType.getFixedLength();
        }
        return barcode.length() >= 4 && barcode.length() <= 64;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    
    public ItemVariant getVariant() {
        return variant;
    }
    
    public void setVariant(ItemVariant variant) {
        this.variant = variant;
        if (variant != null) {
            this.tenantId = variant.getTenant().getId();
        }
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public BarcodeType getBarcodeType() {
        return barcodeType;
    }
    
    public void setBarcodeType(BarcodeType barcodeType) {
        this.barcodeType = barcodeType;
    }
    
    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }
    
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
    
    public PackLevel getPackLevel() {
        return packLevel;
    }
    
    public void setPackLevel(PackLevel packLevel) {
        this.packLevel = packLevel;
    }
    
    public Boolean getIsPrimary() {
        return isPrimary;
    }
    
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    public BarcodeStatus getStatus() {
        return status;
    }
    
    public void setStatus(BarcodeStatus status) {
        this.status = status;
    }
    
    public Map<String, Object> getAiPayload() {
        return aiPayload;
    }
    
    public void setAiPayload(Map<String, Object> aiPayload) {
        this.aiPayload = aiPayload;
    }
    
    public Long getLabelTemplateId() {
        return labelTemplateId;
    }
    
    public void setLabelTemplateId(Long labelTemplateId) {
        this.labelTemplateId = labelTemplateId;
    }
    
    public UserAccount getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UserAccount createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return String.format("ItemBarcode{id=%d, barcode='%s', type=%s, status=%s, isPrimary=%s}", 
                           id, barcode, barcodeType, status, isPrimary);
    }
}