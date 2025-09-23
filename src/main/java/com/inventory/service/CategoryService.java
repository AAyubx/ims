package com.inventory.service;

import com.inventory.entity.Category;
import com.inventory.entity.Department;
import com.inventory.entity.Tenant;
import com.inventory.entity.UserAccount;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Category management
 * Handles CRUD operations and business logic for categories
 */
@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;


    /**
     * Create a new category
     */
    public Category createCategory(Category category, UserAccount currentUser) {
        validateCategoryForCreation(category);
        
        category.setCreatedBy(currentUser);
        category.setUpdatedBy(currentUser);
        
        Category savedCategory = categoryRepository.save(category);
        
        
        return savedCategory;
    }

    /**
     * Update an existing category
     */
    public Category updateCategory(Long categoryId, Category categoryDetails, UserAccount currentUser) {
        Category existingCategory = getCategoryByIdAndTenant(categoryId, categoryDetails.getTenant().getId());
        
        validateCategoryForUpdate(categoryDetails, existingCategory);
        
        // Update fields
        existingCategory.setCode(categoryDetails.getCode());
        existingCategory.setName(categoryDetails.getName());
        existingCategory.setDescription(categoryDetails.getDescription());
        existingCategory.setDepartment(categoryDetails.getDepartment());
        existingCategory.setTaxClassDefault(categoryDetails.getTaxClassDefault());
        existingCategory.setSortOrder(categoryDetails.getSortOrder());
        existingCategory.setUpdatedBy(currentUser);
        
        // Handle parent change
        if (categoryDetails.getParent() != null && !categoryDetails.getParent().equals(existingCategory.getParent())) {
            validateParentChange(existingCategory, categoryDetails.getParent());
            existingCategory.setParent(categoryDetails.getParent());
        }
        
        Category savedCategory = categoryRepository.save(existingCategory);
        
        
        return savedCategory;
    }

    /**
     * Get category by ID and tenant
     */
    @Transactional(readOnly = true)
    public Category getCategoryByIdAndTenant(Long categoryId, Long tenantId) {
        return categoryRepository.findByTenant_IdAndId(tenantId, categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
    }

    /**
     * Get category by code and tenant
     */
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryByCodeAndTenant(String code, Long tenantId) {
        return categoryRepository.findByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Get all categories for a tenant with pagination
     */
    @Transactional(readOnly = true)
    public Page<Category> getCategoriesByTenant(Long tenantId, Pageable pageable) {
        return categoryRepository.findByTenant_Id(tenantId, pageable);
    }

    /**
     * Get all categories for a tenant
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesByTenant(Long tenantId) {
        return categoryRepository.findByTenant_Id(tenantId);
    }

    /**
     * Get categories by department
     */
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByDepartment(Long tenantId, Long departmentId) {
        return categoryRepository.findByTenant_IdAndDepartment_Id(tenantId, departmentId);
    }

    /**
     * Get root categories for a department
     */
    @Transactional(readOnly = true)
    public List<Category> getRootCategoriesByDepartment(Long tenantId, Long departmentId) {
        return categoryRepository.findByTenant_IdAndDepartment_IdAndParentIsNull(tenantId, departmentId);
    }

    /**
     * Get child categories
     */
    @Transactional(readOnly = true)
    public List<Category> getChildCategories(Long tenantId, Long parentCategoryId) {
        return categoryRepository.findByTenant_IdAndParent_Id(tenantId, parentCategoryId);
    }

    /**
     * Search categories by name or code
     */
    @Transactional(readOnly = true)
    public Page<Category> searchCategories(Long tenantId, String searchTerm, Pageable pageable) {
        return categoryRepository.searchByNameOrCode(tenantId, searchTerm, pageable);
    }

    /**
     * Get categories with item count
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCategoriesWithItemCount(Long tenantId) {
        return categoryRepository.findCategoriesWithItemCount(tenantId);
    }

    /**
     * Delete category
     */
    public void deleteCategory(Long categoryId, Long tenantId, UserAccount currentUser) {
        Category category = getCategoryByIdAndTenant(categoryId, tenantId);
        
        validateCategoryForDeletion(category);
        
        categoryRepository.delete(category);
        
    }

    /**
     * Check if category code exists
     */
    @Transactional(readOnly = true)
    public boolean categoryCodeExists(String code, Long tenantId) {
        return categoryRepository.existsByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Check if category code exists excluding specific ID
     */
    @Transactional(readOnly = true)
    public boolean categoryCodeExistsExcludingId(String code, Long tenantId, Long excludeId) {
        return categoryRepository.existsByTenant_IdAndCodeAndIdNot(tenantId, code, excludeId);
    }

    /**
     * Count categories by tenant
     */
    @Transactional(readOnly = true)
    public long countCategoriesByTenant(Long tenantId) {
        return categoryRepository.countByTenant_Id(tenantId);
    }

    // Validation methods

    private void validateCategoryForCreation(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        if (category.getTenant() == null) {
            throw new IllegalArgumentException("Tenant is required");
        }
        
        if (category.getCode() == null || category.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Category code is required");
        }
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        
        // Check for duplicate code
        if (categoryCodeExists(category.getCode(), category.getTenant().getId())) {
            throw new IllegalArgumentException("Category code already exists: " + category.getCode());
        }
        
        // Validate department if provided
        if (category.getDepartment() != null) {
            validateDepartmentBelongsToTenant(category.getDepartment(), category.getTenant());
        }
        
        // Validate parent if provided
        if (category.getParent() != null) {
            validateParentCategory(category.getParent(), category.getTenant());
        }
    }

    private void validateCategoryForUpdate(Category categoryDetails, Category existingCategory) {
        if (categoryDetails == null) {
            throw new IllegalArgumentException("Category details cannot be null");
        }
        
        if (categoryDetails.getCode() == null || categoryDetails.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Category code is required");
        }
        
        if (categoryDetails.getName() == null || categoryDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        
        // Check for duplicate code excluding current category
        if (categoryCodeExistsExcludingId(categoryDetails.getCode(), 
                                        existingCategory.getTenant().getId(), 
                                        existingCategory.getId())) {
            throw new IllegalArgumentException("Category code already exists: " + categoryDetails.getCode());
        }
        
        // Validate department if provided
        if (categoryDetails.getDepartment() != null) {
            validateDepartmentBelongsToTenant(categoryDetails.getDepartment(), existingCategory.getTenant());
        }
    }

    private void validateCategoryForDeletion(Category category) {
        // Check if category has children
        if (category.hasChildren()) {
            throw new IllegalStateException("Cannot delete category with child categories. Please delete or reassign child categories first.");
        }
        
        // Check if category has items
        if (category.hasItems()) {
            throw new IllegalStateException("Cannot delete category with items. Please delete or reassign items first.");
        }
    }

    private void validateParentChange(Category category, Category newParent) {
        if (newParent == null) {
            return;
        }
        
        // Cannot set self as parent
        if (category.getId().equals(newParent.getId())) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
        
        // Check for circular reference (newParent cannot be a descendant of category)
        if (isDescendant(category, newParent)) {
            throw new IllegalArgumentException("Cannot create circular reference in category hierarchy");
        }
        
        // Parent must belong to same tenant
        if (!category.getTenant().getId().equals(newParent.getTenant().getId())) {
            throw new IllegalArgumentException("Parent category must belong to the same tenant");
        }
        
        // Parent must belong to same department (if both have departments)
        if (category.getDepartment() != null && newParent.getDepartment() != null &&
            !category.getDepartment().getId().equals(newParent.getDepartment().getId())) {
            throw new IllegalArgumentException("Parent category must belong to the same department");
        }
    }

    private void validateParentCategory(Category parent, Tenant tenant) {
        if (!parent.getTenant().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("Parent category must belong to the same tenant");
        }
    }

    private void validateDepartmentBelongsToTenant(Department department, Tenant tenant) {
        if (!department.getTenant().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("Department must belong to the same tenant");
        }
    }

    private boolean isDescendant(Category ancestor, Category potentialDescendant) {
        if (potentialDescendant.getParent() == null) {
            return false;
        }
        
        if (potentialDescendant.getParent().getId().equals(ancestor.getId())) {
            return true;
        }
        
        return isDescendant(ancestor, potentialDescendant.getParent());
    }
}