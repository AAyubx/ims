package com.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;

import java.time.LocalDateTime;

/**
 * Entity representing GS1 GTIN allocation configuration for a tenant.
 * Manages company prefix allocation and sequence generation for GTINs.
 */
@Entity
@Table(name = "gs1_configuration",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_gs1_tenant_prefix", columnNames = {"tenant_id", "gs1_prefix"})
       },
       indexes = {
           @Index(name = "idx_gs1_tenant", columnList = "tenant_id"),
           @Index(name = "idx_gs1_active", columnList = "tenant_id, is_active")
       })
public class GS1Configuration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    @NotNull
    private Long tenantId;
    
    @Column(name = "gs1_prefix", nullable = false, length = 10)
    @NotNull
    @Size(min = 3, max = 10, message = "GS1 prefix must be between 3 and 10 digits")
    private String gs1Prefix;
    
    @Column(name = "prefix_capacity", nullable = false)
    @NotNull
    @Min(value = 1, message = "Prefix capacity must be at least 1")
    private Integer prefixCapacity;
    
    @Column(name = "next_sequence", nullable = false)
    @NotNull
    @Min(value = 1, message = "Next sequence must be at least 1")
    private Long nextSequence = 1L;
    
    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @NotNull
    @JsonIgnore
    private UserAccount createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    private UserAccount updatedBy;
    
    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;
    
    // Constructors
    public GS1Configuration() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public GS1Configuration(Long tenantId, String gs1Prefix, Integer prefixCapacity, UserAccount createdBy) {
        this();
        this.tenantId = tenantId;
        this.gs1Prefix = gs1Prefix;
        this.prefixCapacity = prefixCapacity;
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
     * Calculate remaining GTIN capacity for this prefix.
     */
    public long getRemainingCapacity() {
        return Math.max(0, prefixCapacity - (nextSequence - 1));
    }
    
    /**
     * Check if there are available GTINs in this prefix.
     */
    public boolean hasAvailableGTINs() {
        return getRemainingCapacity() > 0 && isActive;
    }
    
    /**
     * Get the next available sequence number and increment the counter.
     */
    public long getNextSequenceAndIncrement() {
        if (!hasAvailableGTINs()) {
            throw new IllegalStateException("No available GTINs for prefix: " + gs1Prefix);
        }
        return nextSequence++;
    }
    
    /**
     * Get capacity utilization percentage.
     */
    public double getCapacityUtilization() {
        if (prefixCapacity == 0) return 0.0;
        return ((double) (nextSequence - 1) / prefixCapacity) * 100.0;
    }
    
    /**
     * Check if capacity is running low (>80% utilized).
     */
    public boolean isCapacityLow() {
        return getCapacityUtilization() > 80.0;
    }
    
    /**
     * Check if capacity is critically low (>95% utilized).
     */
    public boolean isCapacityCritical() {
        return getCapacityUtilization() > 95.0;
    }
    
    /**
     * Generate a GTIN-13 using this prefix configuration.
     */
    public String generateGTIN13() {
        if (!hasAvailableGTINs()) {
            throw new IllegalStateException("No available GTINs for prefix: " + gs1Prefix);
        }
        
        long sequence = getNextSequenceAndIncrement();
        String itemReference = String.format("%0" + (12 - gs1Prefix.length()) + "d", sequence);
        String gtin12 = gs1Prefix + itemReference;
        
        // Calculate and append check digit
        int checkDigit = calculateGTINCheckDigit(gtin12);
        return gtin12 + checkDigit;
    }
    
    /**
     * Generate a GTIN-14 (ITF-14) using this prefix configuration.
     */
    public String generateGTIN14(int packagingIndicator) {
        if (packagingIndicator < 1 || packagingIndicator > 9) {
            throw new IllegalArgumentException("Packaging indicator must be 1-9");
        }
        
        String gtin13 = generateGTIN13();
        String gtin13WithoutCheckDigit = gtin13.substring(0, 12);
        String gtin13WithIndicator = packagingIndicator + gtin13WithoutCheckDigit;
        
        // Calculate new check digit for GTIN-14
        int checkDigit = calculateGTINCheckDigit(gtin13WithIndicator);
        return gtin13WithIndicator + checkDigit;
    }
    
    /**
     * Calculate GTIN check digit using Mod-10 algorithm.
     */
    private int calculateGTINCheckDigit(String digits) {
        int sum = 0;
        boolean odd = true;
        
        // Process digits from right to left
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(digits.charAt(i));
            sum += odd ? digit * 3 : digit;
            odd = !odd;
        }
        
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit;
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
    
    public String getGs1Prefix() {
        return gs1Prefix;
    }
    
    public void setGs1Prefix(String gs1Prefix) {
        this.gs1Prefix = gs1Prefix;
    }
    
    public Integer getPrefixCapacity() {
        return prefixCapacity;
    }
    
    public void setPrefixCapacity(Integer prefixCapacity) {
        this.prefixCapacity = prefixCapacity;
    }
    
    public Long getNextSequence() {
        return nextSequence;
    }
    
    public void setNextSequence(Long nextSequence) {
        this.nextSequence = nextSequence;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public UserAccount getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UserAccount createdBy) {
        this.createdBy = createdBy;
    }
    
    public UserAccount getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(UserAccount updatedBy) {
        this.updatedBy = updatedBy;
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
        return String.format("GS1Configuration{id=%d, tenantId=%d, prefix='%s', capacity=%d, nextSequence=%d, active=%s}", 
                           id, tenantId, gs1Prefix, prefixCapacity, nextSequence, isActive);
    }
}