package com.inventory.service;

import com.inventory.entity.*;
import com.inventory.entity.Item.ItemStatus;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Item management
 * Handles CRUD operations and business logic for items
 */
@Service
@Transactional
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ItemAttributeValueRepository itemAttributeValueRepository;


    /**
     * Create a new item
     */
    public Item createItem(Item item, UserAccount currentUser) {
        validateItemForCreation(item);
        
        item.setCreatedBy(currentUser);
        item.setUpdatedBy(currentUser);
        item.setStatus(ItemStatus.DRAFT); // New items start as draft
        
        Item savedItem = itemRepository.save(item);
        
        
        return savedItem;
    }

    /**
     * Update an existing item
     */
    public Item updateItem(Long itemId, Item itemDetails, UserAccount currentUser) {
        Item existingItem = getItemByIdAndTenant(itemId, itemDetails.getTenant().getId());
        
        validateItemForUpdate(itemDetails, existingItem);
        
        // Update fields
        existingItem.setSku(itemDetails.getSku());
        existingItem.setName(itemDetails.getName());
        existingItem.setShortName(itemDetails.getShortName());
        existingItem.setDescription(itemDetails.getDescription());
        existingItem.setCategory(itemDetails.getCategory());
        existingItem.setDepartment(itemDetails.getDepartment());
        existingItem.setBrand(itemDetails.getBrand());
        existingItem.setItemType(itemDetails.getItemType());
        existingItem.setBaseUom(itemDetails.getBaseUom());
        existingItem.setSellUom(itemDetails.getSellUom());
        existingItem.setBuyUom(itemDetails.getBuyUom());
        existingItem.setHsCode(itemDetails.getHsCode());
        existingItem.setCountryOfOrigin(itemDetails.getCountryOfOrigin());
        existingItem.setTaxClass(itemDetails.getTaxClass());
        existingItem.setIsSerialized(itemDetails.getIsSerialized());
        existingItem.setIsLotTracked(itemDetails.getIsLotTracked());
        existingItem.setShelfLifeDays(itemDetails.getShelfLifeDays());
        existingItem.setSafetyStockDefault(itemDetails.getSafetyStockDefault());
        existingItem.setReorderPointDefault(itemDetails.getReorderPointDefault());
        existingItem.setReorderQuantityDefault(itemDetails.getReorderQuantityDefault());
        existingItem.setStandardCost(itemDetails.getStandardCost());
        existingItem.setMetaTitle(itemDetails.getMetaTitle());
        existingItem.setMetaDescription(itemDetails.getMetaDescription());
        existingItem.setSearchKeywords(itemDetails.getSearchKeywords());
        existingItem.setUpdatedBy(currentUser);
        
        Item savedItem = itemRepository.save(existingItem);
        
        
        return savedItem;
    }

    /**
     * Update item status
     */
    public Item updateItemStatus(Long itemId, Long tenantId, ItemStatus newStatus, UserAccount currentUser) {
        Item item = getItemByIdAndTenant(itemId, tenantId);
        
        validateStatusTransition(item.getStatus(), newStatus);
        
        ItemStatus oldStatus = item.getStatus();
        item.setStatus(newStatus);
        item.setUpdatedBy(currentUser);
        
        Item savedItem = itemRepository.save(item);
        
        
        return savedItem;
    }

    /**
     * Get item by ID and tenant
     */
    @Transactional(readOnly = true)
    public Item getItemByIdAndTenant(Long itemId, Long tenantId) {
        return itemRepository.findByTenant_IdAndId(tenantId, itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with ID: " + itemId));
    }

    /**
     * Get item by SKU and tenant
     */
    @Transactional(readOnly = true)
    public Optional<Item> getItemBySkuAndTenant(String sku, Long tenantId) {
        return itemRepository.findByTenant_IdAndSku(tenantId, sku);
    }

    /**
     * Get all items for a tenant with pagination
     */
    @Transactional(readOnly = true)
    public Page<Item> getItemsByTenant(Long tenantId, Pageable pageable) {
        return itemRepository.findByTenant_Id(tenantId, pageable);
    }

    /**
     * Get items by status
     */
    @Transactional(readOnly = true)
    public Page<Item> getItemsByStatus(Long tenantId, ItemStatus status, Pageable pageable) {
        return itemRepository.findByTenant_IdAndStatus(tenantId, status, pageable);
    }

    /**
     * Get items by category
     */
    @Transactional(readOnly = true)
    public Page<Item> getItemsByCategory(Long tenantId, Long categoryId, Pageable pageable) {
        return itemRepository.findByTenant_IdAndCategory_Id(tenantId, categoryId, pageable);
    }

    /**
     * Get items by department
     */
    @Transactional(readOnly = true)
    public Page<Item> getItemsByDepartment(Long tenantId, Long departmentId, Pageable pageable) {
        return itemRepository.findByTenant_IdAndDepartment_Id(tenantId, departmentId, pageable);
    }

    /**
     * Get items by brand
     */
    @Transactional(readOnly = true)
    public Page<Item> getItemsByBrand(Long tenantId, Long brandId, Pageable pageable) {
        return itemRepository.findByTenant_IdAndBrand_Id(tenantId, brandId, pageable);
    }

    /**
     * Search items by name, SKU, or description
     */
    @Transactional(readOnly = true)
    public Page<Item> searchItems(Long tenantId, String searchTerm, Pageable pageable) {
        return itemRepository.searchByNameSkuOrDescription(tenantId, searchTerm, pageable);
    }

    /**
     * Advanced search with multiple filters
     */
    @Transactional(readOnly = true)
    public Page<Item> advancedSearchItems(Long tenantId, Long departmentId, Long categoryId, 
                                        Long brandId, ItemStatus status, String searchTerm, 
                                        Pageable pageable) {
        return itemRepository.advancedSearch(tenantId, departmentId, categoryId, brandId, 
                                           status, searchTerm, pageable);
    }

    /**
     * Get items with variant count
     */
    @Transactional(readOnly = true)
    public List<Object[]> getItemsWithVariantCount(Long tenantId, ItemStatus status) {
        return itemRepository.findItemsWithVariantCount(tenantId, status);
    }

    /**
     * Get parent items (items that have variants)
     */
    @Transactional(readOnly = true)
    public List<Item> getParentItems(Long tenantId, ItemStatus status) {
        return itemRepository.findParentItems(tenantId, status);
    }

    /**
     * Get items by supplier
     * TODO: Implement when SupplierItem entity and relationships are implemented
     */
    @Transactional(readOnly = true)
    public List<Item> getItemsBySupplier(Long tenantId, Long supplierId) {
        // return itemRepository.findBySupplier(tenantId, supplierId);
        throw new UnsupportedOperationException("Supplier functionality not yet implemented");
    }

    /**
     * Get items needing supplier setup
     * TODO: Implement when SupplierItem entity and relationships are implemented
     */
    @Transactional(readOnly = true)
    public List<Item> getItemsNeedingSupplierSetup(Long tenantId) {
        // return itemRepository.findItemsNeedingSupplierSetup(tenantId);
        throw new UnsupportedOperationException("Supplier functionality not yet implemented");
    }

    /**
     * Get items by attribute value
     */
    @Transactional(readOnly = true)
    public List<Item> getItemsByAttributeValue(Long tenantId, Long attributeId, String value) {
        return itemRepository.findByAttributeValue(tenantId, attributeId, value);
    }

    /**
     * Delete item
     */
    public void deleteItem(Long itemId, Long tenantId, UserAccount currentUser) {
        Item item = getItemByIdAndTenant(itemId, tenantId);
        
        validateItemForDeletion(item);
        
        itemRepository.delete(item);
        
    }

    /**
     * Check if SKU exists
     */
    @Transactional(readOnly = true)
    public boolean skuExists(String sku, Long tenantId) {
        return itemRepository.existsByTenant_IdAndSku(tenantId, sku);
    }

    /**
     * Check if SKU exists excluding specific ID
     */
    @Transactional(readOnly = true)
    public boolean skuExistsExcludingId(String sku, Long tenantId, Long excludeId) {
        return itemRepository.existsByTenant_IdAndSkuAndIdNot(tenantId, sku, excludeId);
    }

    /**
     * Count items by status for a tenant
     */
    @Transactional(readOnly = true)
    public long countItemsByStatus(Long tenantId, ItemStatus status) {
        return itemRepository.countByTenant_IdAndStatus(tenantId, status);
    }

    /**
     * Activate item (change status to ACTIVE)
     */
    public Item activateItem(Long itemId, Long tenantId, UserAccount currentUser) {
        Item item = getItemByIdAndTenant(itemId, tenantId);
        
        validateItemForActivation(item);
        
        return updateItemStatus(itemId, tenantId, ItemStatus.ACTIVE, currentUser);
    }

    /**
     * Discontinue item (change status to DISCONTINUED)
     */
    public Item discontinueItem(Long itemId, Long tenantId, UserAccount currentUser) {
        return updateItemStatus(itemId, tenantId, ItemStatus.DISCONTINUED, currentUser);
    }

    // Validation methods

    private void validateItemForCreation(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        
        if (item.getTenant() == null) {
            throw new IllegalArgumentException("Tenant is required");
        }
        
        if (item.getSku() == null || item.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Item SKU is required");
        }
        
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required");
        }
        
        // Check for duplicate SKU
        if (skuExists(item.getSku(), item.getTenant().getId())) {
            throw new IllegalArgumentException("Item SKU already exists: " + item.getSku());
        }
        
        // Validate category if provided
        if (item.getCategory() != null) {
            validateCategoryBelongsToTenant(item.getCategory(), item.getTenant());
        }
        
        // Validate department if provided
        if (item.getDepartment() != null) {
            validateDepartmentBelongsToTenant(item.getDepartment(), item.getTenant());
        }
        
        // Validate brand if provided
        if (item.getBrand() != null) {
            validateBrandBelongsToTenant(item.getBrand(), item.getTenant());
        }
    }

    private void validateItemForUpdate(Item itemDetails, Item existingItem) {
        if (itemDetails == null) {
            throw new IllegalArgumentException("Item details cannot be null");
        }
        
        if (itemDetails.getSku() == null || itemDetails.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Item SKU is required");
        }
        
        if (itemDetails.getName() == null || itemDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required");
        }
        
        // Check for duplicate SKU excluding current item
        if (skuExistsExcludingId(itemDetails.getSku(), 
                               existingItem.getTenant().getId(), 
                               existingItem.getId())) {
            throw new IllegalArgumentException("Item SKU already exists: " + itemDetails.getSku());
        }
        
        // Validate category if provided
        if (itemDetails.getCategory() != null) {
            validateCategoryBelongsToTenant(itemDetails.getCategory(), existingItem.getTenant());
        }
        
        // Validate department if provided
        if (itemDetails.getDepartment() != null) {
            validateDepartmentBelongsToTenant(itemDetails.getDepartment(), existingItem.getTenant());
        }
        
        // Validate brand if provided
        if (itemDetails.getBrand() != null) {
            validateBrandBelongsToTenant(itemDetails.getBrand(), existingItem.getTenant());
        }
    }

    private void validateItemForDeletion(Item item) {
        // Check if item has variants
        if (item.hasVariants()) {
            throw new IllegalStateException("Cannot delete item with variants. Please delete variants first.");
        }
        
        // Only allow deletion of DRAFT items
        if (item.getStatus() != ItemStatus.DRAFT) {
            throw new IllegalStateException("Can only delete items in DRAFT status. Current status: " + item.getStatus());
        }
    }

    private void validateItemForActivation(Item item) {
        if (item.getStatus() != ItemStatus.DRAFT) {
            throw new IllegalStateException("Can only activate items in DRAFT status. Current status: " + item.getStatus());
        }
        
        // Validate required fields for activation
        if (item.getCategory() == null) {
            throw new IllegalStateException("Category is required to activate item");
        }
        
        if (item.getDepartment() == null) {
            throw new IllegalStateException("Department is required to activate item");
        }
        
        if (item.getBaseUom() == null) {
            throw new IllegalStateException("Base unit of measure is required to activate item");
        }
        
        // Check if all required attributes are provided
        validateRequiredAttributes(item);
    }

    private void validateStatusTransition(ItemStatus currentStatus, ItemStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new IllegalArgumentException("Item is already in " + newStatus + " status");
        }
        
        // Define valid transitions
        switch (currentStatus) {
            case DRAFT:
                if (newStatus != ItemStatus.ACTIVE) {
                    throw new IllegalArgumentException("Items in DRAFT status can only be changed to ACTIVE");
                }
                break;
            case ACTIVE:
                if (newStatus != ItemStatus.DISCONTINUED) {
                    throw new IllegalArgumentException("Items in ACTIVE status can only be changed to DISCONTINUED");
                }
                break;
            case DISCONTINUED:
                if (newStatus != ItemStatus.ACTIVE) {
                    throw new IllegalArgumentException("Items in DISCONTINUED status can only be changed to ACTIVE");
                }
                break;
        }
    }

    private void validateRequiredAttributes(Item item) {
        // This would check if all required attributes for the item's category are provided
        // Implementation would depend on how attribute requirements are defined
        // For now, this is a placeholder
    }

    private void validateCategoryBelongsToTenant(Category category, Tenant tenant) {
        if (!category.getTenant().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("Category must belong to the same tenant");
        }
    }

    private void validateDepartmentBelongsToTenant(Department department, Tenant tenant) {
        if (!department.getTenant().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("Department must belong to the same tenant");
        }
    }

    private void validateBrandBelongsToTenant(Brand brand, Tenant tenant) {
        if (!brand.getTenant().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("Brand must belong to the same tenant");
        }
    }
}