package com.inventory.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AttributeSet entity for associating attributes with categories
 * Defines which attributes are relevant for items in specific categories
 */
@Entity
@Table(name = "attribute_set",
       uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "attribute_definition_id"}),
       indexes = {
           @Index(name = "idx_attr_set_tenant", columnList = "tenant_id"),
           @Index(name = "idx_attr_set_category", columnList = "category_id")
       })
public class AttributeSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    @NotNull
    private AttributeDefinition attributeDefinition;

    @Column(name = "is_required_for_variants", nullable = false)
    private Boolean isRequiredForVariants = false;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserAccount createdBy;

    // Constructors
    public AttributeSet() {}

    public AttributeSet(Tenant tenant, Category category, AttributeDefinition attributeDefinition) {
        this.tenant = tenant;
        this.category = category;
        this.attributeDefinition = attributeDefinition;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public void setAttributeDefinition(AttributeDefinition attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }

    public Boolean getIsRequiredForVariants() {
        return isRequiredForVariants;
    }

    public void setIsRequiredForVariants(Boolean isRequiredForVariants) {
        this.isRequiredForVariants = isRequiredForVariants;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserAccount getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserAccount createdBy) {
        this.createdBy = createdBy;
    }

    // Utility methods
    public boolean isRequired() {
        return isRequiredForVariants || attributeDefinition.getIsRequired();
    }

    public String getAttributeCode() {
        return attributeDefinition != null ? attributeDefinition.getCode() : null;
    }

    public String getAttributeName() {
        return attributeDefinition != null ? attributeDefinition.getName() : null;
    }

    public AttributeDefinition.AttributeDataType getAttributeDataType() {
        return attributeDefinition != null ? attributeDefinition.getDataType() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeSet)) return false;
        AttributeSet that = (AttributeSet) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AttributeSet{" +
                "id=" + id +
                ", categoryCode=" + (category != null ? category.getCode() : null) +
                ", attributeCode=" + (attributeDefinition != null ? attributeDefinition.getCode() : null) +
                ", isRequiredForVariants=" + isRequiredForVariants +
                ", displayOrder=" + displayOrder +
                '}';
    }
}