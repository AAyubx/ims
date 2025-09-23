package com.inventory.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ItemAttributeValue entity for storing attribute values for items
 * Links items to their specific attribute values
 */
@Entity
@Table(name = "item_attribute_value",
       uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "attribute_definition_id"}),
       indexes = {
           @Index(name = "idx_item_attr_tenant", columnList = "tenant_id"),
           @Index(name = "idx_item_attr_def", columnList = "attribute_definition_id")
       })
public class ItemAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @NotNull
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    @NotNull
    private AttributeDefinition attributeDefinition;

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String value;

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
    public ItemAttributeValue() {}

    public ItemAttributeValue(Tenant tenant, Item item, AttributeDefinition attributeDefinition, String value) {
        this.tenant = tenant;
        this.item = item;
        this.attributeDefinition = attributeDefinition;
        this.value = value;
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

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public void setAttributeDefinition(AttributeDefinition attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
    public String getAttributeCode() {
        return attributeDefinition != null ? attributeDefinition.getCode() : null;
    }

    public String getAttributeName() {
        return attributeDefinition != null ? attributeDefinition.getName() : null;
    }

    public AttributeDefinition.AttributeDataType getAttributeDataType() {
        return attributeDefinition != null ? attributeDefinition.getDataType() : null;
    }

    public boolean isNumericValue() {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isBooleanValue() {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    public Double getNumericValue() {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Boolean getBooleanValue() {
        return Boolean.parseBoolean(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemAttributeValue)) return false;
        ItemAttributeValue that = (ItemAttributeValue) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ItemAttributeValue{" +
                "id=" + id +
                ", attributeCode=" + getAttributeCode() +
                ", value='" + value + '\'' +
                '}';
    }
}