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
 * Category entity representing product categories in a hierarchical structure
 * Categories belong to departments and can have parent-child relationships
 */
@Entity
@Table(name = "category", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "code"}),
       indexes = {
           @Index(name = "idx_cat_tenant", columnList = "tenant_id"),
           @Index(name = "idx_cat_department", columnList = "department_id"),
           @Index(name = "idx_cat_parent", columnList = "parent_id"),
           @Index(name = "idx_cat_leaf", columnList = "is_leaf")
       })
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "code", nullable = false, length = 64)
    @NotBlank
    @Size(min = 2, max = 64)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(name = "tax_class_default", length = 32)
    private String taxClassDefault;

    @Column(name = "is_leaf", nullable = false)
    private Boolean isLeaf = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

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
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> children = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Item> items = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AttributeSet> attributeSets = new HashSet<>();

    // Constructors
    public Category() {}

    public Category(Tenant tenant, String code, String name) {
        this.tenant = tenant;
        this.code = code;
        this.name = name;
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
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

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
        // Update isLeaf status of parent
        if (parent != null) {
            parent.setIsLeaf(false);
        }
    }

    public String getTaxClassDefault() {
        return taxClassDefault;
    }

    public void setTaxClassDefault(String taxClassDefault) {
        this.taxClassDefault = taxClassDefault;
    }

    public Boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(Boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public Set<Category> getChildren() {
        return children;
    }

    public void setChildren(Set<Category> children) {
        this.children = children;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    public Set<AttributeSet> getAttributeSets() {
        return attributeSets;
    }

    public void setAttributeSets(Set<AttributeSet> attributeSets) {
        this.attributeSets = attributeSets;
    }

    // Utility methods
    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
        this.isLeaf = false;
    }

    public void removeChild(Category child) {
        children.remove(child);
        child.setParent(null);
        if (children.isEmpty()) {
            this.isLeaf = true;
        }
    }

    public void addItem(Item item) {
        items.add(item);
        item.setCategory(this);
    }

    public void removeItem(Item item) {
        items.remove(item);
        item.setCategory(null);
    }

    public void addAttributeSet(AttributeSet attributeSet) {
        attributeSets.add(attributeSet);
        attributeSet.setCategory(this);
    }

    public void removeAttributeSet(AttributeSet attributeSet) {
        attributeSets.remove(attributeSet);
        attributeSet.setCategory(null);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    public String getDepartmentCode() {
        return department != null ? department.getCode() : null;
    }

    public String getDepartmentName() {
        return department != null ? department.getName() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return id != null && id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", isLeaf=" + isLeaf +
                '}';
    }
}