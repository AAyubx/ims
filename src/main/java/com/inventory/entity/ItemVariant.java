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
 * ItemVariant entity representing sellable SKUs/variants of items
 * Variants are the actual sellable units with specific attributes like color, size, etc.
 */
@Entity
@Table(name = "item_variant", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "variant_sku"}),
       indexes = {
           @Index(name = "idx_variant_item", columnList = "item_id"),
           @Index(name = "idx_variant_tenant", columnList = "tenant_id"),
           @Index(name = "idx_variant_upc", columnList = "upc"),
           @Index(name = "idx_variant_ean", columnList = "ean"),
           @Index(name = "idx_variant_gtin", columnList = "gtin"),
           @Index(name = "idx_variant_status_active", columnList = "status, is_active_for_sale, is_active_for_purchase")
       })
public class ItemVariant {

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

    @Column(name = "variant_sku", nullable = false, length = 64)
    @NotBlank
    @Size(min = 2, max = 64)
    private String variantSku;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "short_name", length = 128)
    private String shortName;

    @Column(name = "upc", length = 64)
    private String upc;

    @Column(name = "ean", length = 64)
    private String ean;

    @Column(name = "gtin", length = 64)
    private String gtin;

    @Column(name = "attributes_json", columnDefinition = "JSON")
    private String attributesJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_uom_id")
    private UnitOfMeasure baseUom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_uom_id")
    private UnitOfMeasure sellUom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_uom_id")
    private UnitOfMeasure buyUom;

    @Column(name = "weight_value", precision = 10, scale = 3)
    private BigDecimal weightValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weight_uom_id")
    private UnitOfMeasure weightUom;

    @Column(name = "volume_value", precision = 10, scale = 3)
    private BigDecimal volumeValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volume_uom_id")
    private UnitOfMeasure volumeUom;

    @Column(name = "length_value", precision = 10, scale = 3)
    private BigDecimal lengthValue;

    @Column(name = "width_value", precision = 10, scale = 3)
    private BigDecimal widthValue;

    @Column(name = "height_value", precision = 10, scale = 3)
    private BigDecimal heightValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_uom_id")
    private UnitOfMeasure dimensionUom;

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

    @Column(name = "standard_cost", precision = 12, scale = 4)
    private BigDecimal standardCost;

    @Column(name = "last_cost", precision = 12, scale = 4)
    private BigDecimal lastCost;

    @Column(name = "average_cost", precision = 12, scale = 4)
    private BigDecimal averageCost;

    @Column(name = "base_price", precision = 12, scale = 4)
    private BigDecimal basePrice;

    @Column(name = "is_active_for_sale", nullable = false)
    private Boolean isActiveForSale = true;

    @Column(name = "is_active_for_purchase", nullable = false)
    private Boolean isActiveForPurchase = true;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private VariantStatus status = VariantStatus.ACTIVE;

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
    // @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Set<ItemBarcode> barcodes = new HashSet<>();

    // @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Set<SupplierItem> supplierItems = new HashSet<>();

    // Constructors
    public ItemVariant() {}

    public ItemVariant(Tenant tenant, Item item, String variantSku) {
        this.tenant = tenant;
        this.item = item;
        this.variantSku = variantSku;
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

    public String getVariantSku() {
        return variantSku;
    }

    public void setVariantSku(String variantSku) {
        this.variantSku = variantSku;
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

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }

    public String getAttributesJson() {
        return attributesJson;
    }

    public void setAttributesJson(String attributesJson) {
        this.attributesJson = attributesJson;
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

    public BigDecimal getWeightValue() {
        return weightValue;
    }

    public void setWeightValue(BigDecimal weightValue) {
        this.weightValue = weightValue;
    }

    public UnitOfMeasure getWeightUom() {
        return weightUom;
    }

    public void setWeightUom(UnitOfMeasure weightUom) {
        this.weightUom = weightUom;
    }

    public BigDecimal getVolumeValue() {
        return volumeValue;
    }

    public void setVolumeValue(BigDecimal volumeValue) {
        this.volumeValue = volumeValue;
    }

    public UnitOfMeasure getVolumeUom() {
        return volumeUom;
    }

    public void setVolumeUom(UnitOfMeasure volumeUom) {
        this.volumeUom = volumeUom;
    }

    public BigDecimal getLengthValue() {
        return lengthValue;
    }

    public void setLengthValue(BigDecimal lengthValue) {
        this.lengthValue = lengthValue;
    }

    public BigDecimal getWidthValue() {
        return widthValue;
    }

    public void setWidthValue(BigDecimal widthValue) {
        this.widthValue = widthValue;
    }

    public BigDecimal getHeightValue() {
        return heightValue;
    }

    public void setHeightValue(BigDecimal heightValue) {
        this.heightValue = heightValue;
    }

    public UnitOfMeasure getDimensionUom() {
        return dimensionUom;
    }

    public void setDimensionUom(UnitOfMeasure dimensionUom) {
        this.dimensionUom = dimensionUom;
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

    public Boolean getIsActiveForSale() {
        return isActiveForSale;
    }

    public void setIsActiveForSale(Boolean isActiveForSale) {
        this.isActiveForSale = isActiveForSale;
    }

    public Boolean getIsActiveForPurchase() {
        return isActiveForPurchase;
    }

    public void setIsActiveForPurchase(Boolean isActiveForPurchase) {
        this.isActiveForPurchase = isActiveForPurchase;
    }

    public VariantStatus getStatus() {
        return status;
    }

    public void setStatus(VariantStatus status) {
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

    // public Set<ItemBarcode> getBarcodes() {
    //     return barcodes;
    // }

    // public void setBarcodes(Set<ItemBarcode> barcodes) {
    //     this.barcodes = barcodes;
    // }

    // public Set<SupplierItem> getSupplierItems() {
    //     return supplierItems;
    // }

    // public void setSupplierItems(Set<SupplierItem> supplierItems) {
    //     this.supplierItems = supplierItems;
    // }

    // Utility methods
    // public void addBarcode(ItemBarcode barcode) {
    //     barcodes.add(barcode);
    //     barcode.setVariant(this);
    // }

    // public void removeBarcode(ItemBarcode barcode) {
    //     barcodes.remove(barcode);
    //     barcode.setVariant(null);
    // }

    // public void addSupplierItem(SupplierItem supplierItem) {
    //     supplierItems.add(supplierItem);
    //     supplierItem.setVariant(this);
    // }

    // public void removeSupplierItem(SupplierItem supplierItem) {
    //     supplierItems.remove(supplierItem);
    //     supplierItem.setVariant(null);
    // }

    public boolean isActive() {
        return status == VariantStatus.ACTIVE;
    }

    public boolean isDiscontinued() {
        return status == VariantStatus.DISCONTINUED;
    }

    public boolean canSell() {
        return isActive() && isActiveForSale;
    }

    public boolean canPurchase() {
        return isActive() && isActiveForPurchase;
    }

    public String getItemName() {
        return item != null ? item.getName() : null;
    }

    public String getItemSku() {
        return item != null ? item.getSku() : null;
    }

    public String getEffectiveName() {
        return name != null ? name : getItemName();
    }

    public String getPrimaryBarcode() {
        // return barcodes.stream()
        //         .filter(b -> b.getIsPrimary())
        //         .map(ItemBarcode::getBarcode)
        //         .findFirst()
        //         .orElse(upc != null ? upc : ean);
        return upc != null ? upc : ean;
    }

    public boolean hasDimensions() {
        return lengthValue != null && widthValue != null && heightValue != null;
    }

    public boolean hasWeight() {
        return weightValue != null;
    }

    public boolean hasVolume() {
        return volumeValue != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemVariant)) return false;
        ItemVariant that = (ItemVariant) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ItemVariant{" +
                "id=" + id +
                ", variantSku='" + variantSku + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

    /**
     * Variant status enumeration
     */
    public enum VariantStatus {
        ACTIVE,        // Active variant available for operations
        DISCONTINUED   // Discontinued variant
    }
}