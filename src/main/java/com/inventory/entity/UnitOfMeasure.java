package com.inventory.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit of Measure entity for defining measurement units
 * (e.g., EA, BOX, CASE, KG, G, L, ML, etc.)
 */
@Entity
@Table(name = "unit_of_measure", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "code"}),
       indexes = {
           @Index(name = "idx_uom_tenant_type", columnList = "tenant_id, unit_type")
       })
public class UnitOfMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private Tenant tenant;

    @Column(name = "code", nullable = false, length = 10)
    @NotBlank
    @Size(min = 1, max = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 64)
    @NotBlank
    @Size(min = 2, max = 64)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private UnitType unitType;

    @Column(name = "is_base_unit", nullable = false)
    private Boolean isBaseUnit = false;

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

    // Relationships
    @OneToMany(mappedBy = "fromUom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UomConversion> conversionsFrom = new HashSet<>();

    @OneToMany(mappedBy = "toUom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UomConversion> conversionsTo = new HashSet<>();

    // Constructors
    public UnitOfMeasure() {}

    public UnitOfMeasure(Tenant tenant, String code, String name, UnitType unitType) {
        this.tenant = tenant;
        this.code = code;
        this.name = name;
        this.unitType = unitType;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public Boolean getIsBaseUnit() {
        return isBaseUnit;
    }

    public void setIsBaseUnit(Boolean isBaseUnit) {
        this.isBaseUnit = isBaseUnit;
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

    public Set<UomConversion> getConversionsFrom() {
        return conversionsFrom;
    }

    public void setConversionsFrom(Set<UomConversion> conversionsFrom) {
        this.conversionsFrom = conversionsFrom;
    }

    public Set<UomConversion> getConversionsTo() {
        return conversionsTo;
    }

    public void setConversionsTo(Set<UomConversion> conversionsTo) {
        this.conversionsTo = conversionsTo;
    }

    // Utility methods
    public boolean isCountType() {
        return unitType == UnitType.COUNT;
    }

    public boolean isWeightType() {
        return unitType == UnitType.WEIGHT;
    }

    public boolean isVolumeType() {
        return unitType == UnitType.VOLUME;
    }

    public boolean isLengthType() {
        return unitType == UnitType.LENGTH;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnitOfMeasure)) return false;
        UnitOfMeasure that = (UnitOfMeasure) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UnitOfMeasure{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", unitType=" + unitType +
                ", isBaseUnit=" + isBaseUnit +
                '}';
    }

    /**
     * Unit type enumeration
     */
    public enum UnitType {
        COUNT,    // Each, Box, Case, Piece
        WEIGHT,   // Kilogram, Gram, Pound, Ounce
        VOLUME,   // Liter, Milliliter, Gallon, Fluid Ounce
        LENGTH,   // Meter, Centimeter, Inch, Foot
        AREA,     // Square meter, Square foot
        TIME      // Hour, Day, Week, Month
    }
}