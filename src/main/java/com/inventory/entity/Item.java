package com.inventory.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Item entity representing parent items/styles in the catalog
 * Items can be simple items or parent items with variants
 */
@Entity
@Table(name = "item", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "sku"}),
       indexes = {
           @Index(name = "idx_item_cat", columnList = "category_id"),
           @Index(name = "idx_item_tenant", columnList = "tenant_id"),
           @Index(name = "idx_item_department", columnList = "department_id"),
           @Index(name = "idx_item_brand", columnList = "brand_id"),
           @Index(name = "idx_item_type", columnList = "item_type"),
           @Index(name = "idx_item_status_type", columnList = "status, item_type")
       })
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private Tenant tenant;

    @Column(name = "sku", nullable = false, length = 64)
    @NotBlank
    @Size(min = 2, max = 64)
    private String sku;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @Column(name = "short_name", length = 128)
    private String shortName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "brand", length = 128)
    private String brandName; // Legacy field for backward compatibility

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "item_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ItemType itemType = ItemType.SIMPLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_uom_id")
    private UnitOfMeasure baseUom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_uom_id")
    private UnitOfMeasure sellUom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_uom_id")
    private UnitOfMeasure buyUom;

    @Column(name = "tax_class", length = 32)
    private String taxClass;

    @Column(name = "hs_code", length = 32)
    private String hsCode;

    @Column(name = "country_of_origin", length = 2)
    private String countryOfOrigin;

    @Column(name = "is_serialized", nullable = false)
    private Boolean isSerialized = false;

    @Column(name = "is_lot_tracked", nullable = false)
    private Boolean isLotTracked = false;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Column(name = "safety_stock_default", nullable = false)
    private Integer safetyStockDefault = 0;

    @Column(name = "reorder_point_default", nullable = false)
    private Integer reorderPointDefault = 0;

    @Column(name = "reorder_quantity_default", nullable = false)
    private Integer reorderQuantityDefault = 0;

    @Column(name = "standard_cost", precision = 12, scale = 4)
    private BigDecimal standardCost;

    @Column(name = "last_cost", precision = 12, scale = 4)
    private BigDecimal lastCost;

    @Column(name = "average_cost", precision = 12, scale = 4)
    private BigDecimal averageCost;

    @Column(name = "base_price", precision = 12, scale = 4)
    private BigDecimal basePrice;

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @Column(name = "search_keywords", columnDefinition = "TEXT")
    private String searchKeywords;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.DRAFT;

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
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ItemVariant> variants = new HashSet<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ItemAttributeValue> attributeValues = new HashSet<>();

    // @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Set<ItemMedia> mediaFiles = new HashSet<>();

    // Constructors
    public Item() {}

    public Item(Tenant tenant, String sku, String name) {
        this.tenant = tenant;
        this.sku = sku;
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public UnitOfMeasure getBaseUom() {
        return baseUom;
    }

    public void setBaseUom(UnitOfMeasure baseUom) {
        this.baseUom = baseUom;
    }

    public UnitOfMeasure getSellUom() {
        return sellUom;
    }

    public void setSellUom(UnitOfMeasure sellUom) {
        this.sellUom = sellUom;
    }

    public UnitOfMeasure getBuyUom() {
        return buyUom;
    }

    public void setBuyUom(UnitOfMeasure buyUom) {
        this.buyUom = buyUom;
    }

    public String getTaxClass() {
        return taxClass;
    }

    public void setTaxClass(String taxClass) {
        this.taxClass = taxClass;
    }

    public String getHsCode() {
        return hsCode;
    }

    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public Boolean getIsSerialized() {
        return isSerialized;
    }

    public void setIsSerialized(Boolean isSerialized) {
        this.isSerialized = isSerialized;
    }

    public Boolean getIsLotTracked() {
        return isLotTracked;
    }

    public void setIsLotTracked(Boolean isLotTracked) {
        this.isLotTracked = isLotTracked;
    }

    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(Integer shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
    }

    public Integer getSafetyStockDefault() {
        return safetyStockDefault;
    }

    public void setSafetyStockDefault(Integer safetyStockDefault) {
        this.safetyStockDefault = safetyStockDefault;
    }

    public Integer getReorderPointDefault() {
        return reorderPointDefault;
    }

    public void setReorderPointDefault(Integer reorderPointDefault) {
        this.reorderPointDefault = reorderPointDefault;
    }

    public Integer getReorderQuantityDefault() {
        return reorderQuantityDefault;
    }

    public void setReorderQuantityDefault(Integer reorderQuantityDefault) {
        this.reorderQuantityDefault = reorderQuantityDefault;
    }

    public BigDecimal getStandardCost() {
        return standardCost;
    }

    public void setStandardCost(BigDecimal standardCost) {
        this.standardCost = standardCost;
    }

    public BigDecimal getLastCost() {
        return lastCost;
    }

    public void setLastCost(BigDecimal lastCost) {
        this.lastCost = lastCost;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getSearchKeywords() {
        return searchKeywords;
    }

    public void setSearchKeywords(String searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
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

    public Set<ItemVariant> getVariants() {
        return variants;
    }

    public void setVariants(Set<ItemVariant> variants) {
        this.variants = variants;
    }

    public Set<ItemAttributeValue> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(Set<ItemAttributeValue> attributeValues) {
        this.attributeValues = attributeValues;
    }

    // public Set<ItemMedia> getMediaFiles() {
    //     return mediaFiles;
    // }

    // public void setMediaFiles(Set<ItemMedia> mediaFiles) {
    //     this.mediaFiles = mediaFiles;
    // }

    // Utility methods
    public void addVariant(ItemVariant variant) {
        variants.add(variant);
        variant.setItem(this);
    }

    public void removeVariant(ItemVariant variant) {
        variants.remove(variant);
        variant.setItem(null);
    }

    public void addAttributeValue(ItemAttributeValue attributeValue) {
        attributeValues.add(attributeValue);
        attributeValue.setItem(this);
    }

    public void removeAttributeValue(ItemAttributeValue attributeValue) {
        attributeValues.remove(attributeValue);
        attributeValue.setItem(null);
    }

    // public void addMediaFile(ItemMedia media) {
    //     mediaFiles.add(media);
    //     media.setItem(this);
    // }

    // public void removeMediaFile(ItemMedia media) {
    //     mediaFiles.remove(media);
    //     media.setItem(null);
    // }

    public boolean hasVariants() {
        return !variants.isEmpty();
    }

    public boolean isParentItem() {
        return itemType == ItemType.PARENT;
    }

    public boolean isSimpleItem() {
        return itemType == ItemType.SIMPLE;
    }

    public boolean isBundle() {
        return itemType == ItemType.BUNDLE;
    }

    public boolean isKit() {
        return itemType == ItemType.KIT;
    }

    public boolean isActive() {
        return status == ItemStatus.ACTIVE;
    }

    public boolean isDraft() {
        return status == ItemStatus.DRAFT;
    }

    public boolean isDiscontinued() {
        return status == ItemStatus.DISCONTINUED;
    }

    public String getEffectiveBrandName() {
        return brand != null ? brand.getName() : brandName;
    }

    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public String getDepartmentName() {
        return department != null ? department.getName() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return id != null && id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", itemType=" + itemType +
                ", status=" + status +
                '}';
    }

    /**
     * Item type enumeration
     */
    public enum ItemType {
        SIMPLE,  // Single item without variants
        PARENT,  // Parent item with variants/children
        BUNDLE,  // Bundle of multiple items
        KIT      // Kit with components
    }

    /**
     * Item status enumeration
     */
    public enum ItemStatus {
        DRAFT,        // Item being created/edited
        ACTIVE,       // Active item available for sale
        DISCONTINUED  // Discontinued item
    }
}