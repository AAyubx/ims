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
 * AttributeDefinition entity for defining item attributes
 * (e.g., Color, Size, Material, Gender, etc.)
 */
@Entity
@Table(name = "attribute_definition", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "code"}),
       indexes = {
           @Index(name = "idx_attr_tenant_type", columnList = "tenant_id, data_type")
       })
public class AttributeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private Tenant tenant;

    @Column(name = "code", nullable = false, length = 32)
    @NotBlank
    @Size(min = 2, max = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    @NotBlank
    @Size(min = 2, max = 128)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "data_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AttributeDataType dataType;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "allowed_values", columnDefinition = "JSON")
    private String allowedValues; // JSON array for LIST type attributes

    @Column(name = "validation_rules", columnDefinition = "JSON")
    private String validationRules; // Additional validation rules as JSON

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
    @OneToMany(mappedBy = "attributeDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AttributeSet> attributeSets = new HashSet<>();

    @OneToMany(mappedBy = "attributeDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ItemAttributeValue> itemAttributeValues = new HashSet<>();

    // Constructors
    public AttributeDefinition() {}

    public AttributeDefinition(Tenant tenant, String code, String name, AttributeDataType dataType) {
        this.tenant = tenant;
        this.code = code;
        this.name = name;
        this.dataType = dataType;
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

    public AttributeDataType getDataType() {
        return dataType;
    }

    public void setDataType(AttributeDataType dataType) {
        this.dataType = dataType;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public String getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(String allowedValues) {
        this.allowedValues = allowedValues;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
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

    public Set<AttributeSet> getAttributeSets() {
        return attributeSets;
    }

    public void setAttributeSets(Set<AttributeSet> attributeSets) {
        this.attributeSets = attributeSets;
    }

    public Set<ItemAttributeValue> getItemAttributeValues() {
        return itemAttributeValues;
    }

    public void setItemAttributeValues(Set<ItemAttributeValue> itemAttributeValues) {
        this.itemAttributeValues = itemAttributeValues;
    }

    // Utility methods
    public boolean isListType() {
        return dataType == AttributeDataType.LIST;
    }

    public boolean isNumericType() {
        return dataType == AttributeDataType.NUMBER;
    }

    public boolean isBooleanType() {
        return dataType == AttributeDataType.BOOLEAN;
    }

    public boolean isTextType() {
        return dataType == AttributeDataType.TEXT;
    }

    public boolean isDateType() {
        return dataType == AttributeDataType.DATE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeDefinition)) return false;
        AttributeDefinition that = (AttributeDefinition) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AttributeDefinition{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", dataType=" + dataType +
                ", isRequired=" + isRequired +
                '}';
    }

    /**
     * Attribute data type enumeration
     */
    public enum AttributeDataType {
        TEXT,      // Free text input
        NUMBER,    // Numeric input (integer or decimal)
        BOOLEAN,   // True/false checkbox
        LIST,      // Dropdown/select from predefined values
        DATE       // Date picker
    }
}