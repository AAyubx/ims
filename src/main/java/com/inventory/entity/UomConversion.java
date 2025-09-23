package com.inventory.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UoM Conversion entity for defining conversion factors between units
 * (e.g., 1 KG = 1000 G, 1 L = 1000 ML)
 */
@Entity
@Table(name = "uom_conversion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"from_uom_id", "to_uom_id"}))
public class UomConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_uom_id", nullable = false)
    @NotNull
    private UnitOfMeasure fromUom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_uom_id", nullable = false)
    @NotNull
    private UnitOfMeasure toUom;

    @Column(name = "conversion_factor", nullable = false, precision = 12, scale = 6)
    @NotNull
    @Positive
    private BigDecimal conversionFactor;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserAccount createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserAccount updatedBy;

    // Constructors
    public UomConversion() {}

    public UomConversion(Tenant tenant, UnitOfMeasure fromUom, UnitOfMeasure toUom, BigDecimal conversionFactor) {
        this.tenant = tenant;
        this.fromUom = fromUom;
        this.toUom = toUom;
        this.conversionFactor = conversionFactor;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public UnitOfMeasure getFromUom() {
        return fromUom;
    }

    public void setFromUom(UnitOfMeasure fromUom) {
        this.fromUom = fromUom;
    }

    public UnitOfMeasure getToUom() {
        return toUom;
    }

    public void setToUom(UnitOfMeasure toUom) {
        this.toUom = toUom;
    }

    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }

    public void setConversionFactor(BigDecimal conversionFactor) {
        this.conversionFactor = conversionFactor;
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

    // Utility methods
    public BigDecimal convert(BigDecimal quantity) {
        if (quantity == null) {
            return null;
        }
        return quantity.multiply(conversionFactor);
    }

    public String getFromUomCode() {
        return fromUom != null ? fromUom.getCode() : null;
    }

    public String getToUomCode() {
        return toUom != null ? toUom.getCode() : null;
    }

    public boolean isSameUnitType() {
        return fromUom != null && toUom != null && 
               fromUom.getUnitType() == toUom.getUnitType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UomConversion)) return false;
        UomConversion that = (UomConversion) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UomConversion{" +
                "id=" + id +
                ", fromUom=" + getFromUomCode() +
                ", toUom=" + getToUomCode() +
                ", conversionFactor=" + conversionFactor +
                '}';
    }
}