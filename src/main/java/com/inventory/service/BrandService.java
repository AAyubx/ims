package com.inventory.service;

import com.inventory.entity.Brand;
import com.inventory.entity.Brand.BrandStatus;
import com.inventory.entity.UserAccount;
import com.inventory.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Brand management
 * Handles CRUD operations and business logic for brands
 */
@Service
@Transactional
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;


    /**
     * Create a new brand
     */
    public Brand createBrand(Brand brand, UserAccount currentUser) {
        validateBrandForCreation(brand);
        
        brand.setCreatedBy(currentUser);
        brand.setUpdatedBy(currentUser);
        brand.setStatus(BrandStatus.ACTIVE); // New brands are active by default
        
        Brand savedBrand = brandRepository.save(brand);
        
        
        return savedBrand;
    }

    /**
     * Update an existing brand
     */
    public Brand updateBrand(Long brandId, Brand brandDetails, UserAccount currentUser) {
        Brand existingBrand = getBrandByIdAndTenant(brandId, brandDetails.getTenant().getId());
        
        validateBrandForUpdate(brandDetails, existingBrand);
        
        // Update fields
        existingBrand.setCode(brandDetails.getCode());
        existingBrand.setName(brandDetails.getName());
        existingBrand.setDescription(brandDetails.getDescription());
        existingBrand.setVendor(brandDetails.getVendor());
        existingBrand.setLogoUrl(brandDetails.getLogoUrl());
        existingBrand.setUpdatedBy(currentUser);
        
        Brand savedBrand = brandRepository.save(existingBrand);
        
        
        return savedBrand;
    }

    /**
     * Get brand by ID and tenant
     */
    @Transactional(readOnly = true)
    public Brand getBrandByIdAndTenant(Long brandId, Long tenantId) {
        return brandRepository.findByTenant_IdAndId(tenantId, brandId)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with ID: " + brandId));
    }

    /**
     * Get brand by code and tenant
     */
    @Transactional(readOnly = true)
    public Optional<Brand> getBrandByCodeAndTenant(String code, Long tenantId) {
        return brandRepository.findByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Get all brands for a tenant with pagination
     */
    @Transactional(readOnly = true)
    public Page<Brand> getBrandsByTenant(Long tenantId, Pageable pageable) {
        return brandRepository.findByTenant_Id(tenantId, pageable);
    }

    /**
     * Get all active brands for a tenant
     */
    @Transactional(readOnly = true)
    public List<Brand> getActiveBrandsByTenant(Long tenantId) {
        return brandRepository.findByTenant_IdAndStatus(tenantId, BrandStatus.ACTIVE);
    }

    /**
     * Get brands by status
     */
    @Transactional(readOnly = true)
    public Page<Brand> getBrandsByStatus(Long tenantId, BrandStatus status, Pageable pageable) {
        return brandRepository.findByTenant_IdAndStatus(tenantId, status, pageable);
    }

    /**
     * Search brands by name or code
     */
    @Transactional(readOnly = true)
    public Page<Brand> searchBrands(Long tenantId, String searchTerm, Pageable pageable) {
        return brandRepository.searchByNameOrCode(tenantId, searchTerm, pageable);
    }

    /**
     * Delete brand
     */
    public void deleteBrand(Long brandId, Long tenantId, UserAccount currentUser) {
        Brand brand = getBrandByIdAndTenant(brandId, tenantId);
        
        validateBrandForDeletion(brand);
        
        brandRepository.delete(brand);
        
    }

    /**
     * Check if brand code exists
     */
    @Transactional(readOnly = true)
    public boolean brandCodeExists(String code, Long tenantId) {
        return brandRepository.existsByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Check if brand code exists excluding specific ID
     */
    @Transactional(readOnly = true)
    public boolean brandCodeExistsExcludingId(String code, Long tenantId, Long excludeId) {
        return brandRepository.existsByTenant_IdAndCodeAndIdNot(tenantId, code, excludeId);
    }

    /**
     * Count brands by status for a tenant
     */
    @Transactional(readOnly = true)
    public long countBrandsByStatus(Long tenantId, BrandStatus status) {
        return brandRepository.countByTenant_IdAndStatus(tenantId, status);
    }

    /**
     * Activate brand
     */
    public Brand activateBrand(Long brandId, Long tenantId, UserAccount currentUser) {
        Brand brand = getBrandByIdAndTenant(brandId, tenantId);
        brand.setStatus(BrandStatus.ACTIVE);
        brand.setUpdatedBy(currentUser);
        
        Brand savedBrand = brandRepository.save(brand);
        
        
        return savedBrand;
    }

    /**
     * Deactivate brand
     */
    public Brand deactivateBrand(Long brandId, Long tenantId, UserAccount currentUser) {
        Brand brand = getBrandByIdAndTenant(brandId, tenantId);
        brand.setStatus(BrandStatus.INACTIVE);
        brand.setUpdatedBy(currentUser);
        
        Brand savedBrand = brandRepository.save(brand);
        
        
        return savedBrand;
    }

    // Validation methods

    private void validateBrandForCreation(Brand brand) {
        if (brand == null) {
            throw new IllegalArgumentException("Brand cannot be null");
        }
        
        if (brand.getTenant() == null) {
            throw new IllegalArgumentException("Tenant is required");
        }
        
        if (brand.getCode() == null || brand.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Brand code is required");
        }
        
        if (brand.getName() == null || brand.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Brand name is required");
        }
        
        // Check for duplicate code
        if (brandCodeExists(brand.getCode(), brand.getTenant().getId())) {
            throw new IllegalArgumentException("Brand code already exists: " + brand.getCode());
        }
    }

    private void validateBrandForUpdate(Brand brandDetails, Brand existingBrand) {
        if (brandDetails == null) {
            throw new IllegalArgumentException("Brand details cannot be null");
        }
        
        if (brandDetails.getCode() == null || brandDetails.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Brand code is required");
        }
        
        if (brandDetails.getName() == null || brandDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Brand name is required");
        }
        
        // Check for duplicate code excluding current brand
        if (brandCodeExistsExcludingId(brandDetails.getCode(), 
                                     existingBrand.getTenant().getId(), 
                                     existingBrand.getId())) {
            throw new IllegalArgumentException("Brand code already exists: " + brandDetails.getCode());
        }
    }

    private void validateBrandForDeletion(Brand brand) {
        // Check if brand has items
        if (brand.getItems() != null && !brand.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot delete brand with items. Please delete or reassign items first.");
        }
        
        // Check if brand is active
        if (brand.getStatus() == BrandStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete active brand. Please deactivate first.");
        }
    }
}