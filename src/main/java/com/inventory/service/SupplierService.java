package com.inventory.service;

import com.inventory.entity.Supplier;
import com.inventory.entity.Tenant;
import com.inventory.entity.UserAccount;
import com.inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Supplier management
 * Handles CRUD operations and business logic for suppliers
 */
@Service
@Transactional
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;


    /**
     * Create a new supplier
     */
    public Supplier createSupplier(Supplier supplier, UserAccount currentUser) {
        validateSupplierForCreation(supplier);
        
        supplier.setCreatedBy(currentUser);
        supplier.setUpdatedBy(currentUser);
        
        Supplier savedSupplier = supplierRepository.save(supplier);
        
        
        return savedSupplier;
    }

    /**
     * Update an existing supplier
     */
    public Supplier updateSupplier(Long supplierId, Supplier supplierDetails, UserAccount currentUser) {
        Supplier existingSupplier = getSupplierByIdAndTenant(supplierId, supplierDetails.getTenant().getId());
        
        validateSupplierForUpdate(supplierDetails, existingSupplier);
        
        // Update fields
        existingSupplier.setCode(supplierDetails.getCode());
        existingSupplier.setName(supplierDetails.getName());
        existingSupplier.setContactEmail(supplierDetails.getContactEmail());
        existingSupplier.setStatus(supplierDetails.getStatus());
        
        Supplier savedSupplier = supplierRepository.save(existingSupplier);
        
        
        return savedSupplier;
    }

    /**
     * Get supplier by ID and tenant
     */
    @Transactional(readOnly = true)
    public Supplier getSupplierByIdAndTenant(Long supplierId, Long tenantId) {
        return supplierRepository.findByTenant_IdAndId(tenantId, supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + supplierId));
    }

    /**
     * Get supplier by code and tenant
     */
    @Transactional(readOnly = true)
    public Optional<Supplier> getSupplierByCodeAndTenant(String code, Long tenantId) {
        return supplierRepository.findByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Get all suppliers for a tenant with pagination
     */
    @Transactional(readOnly = true)
    public Page<Supplier> getSuppliersByTenant(Long tenantId, Pageable pageable) {
        return supplierRepository.findByTenant_Id(tenantId, pageable);
    }

    /**
     * Get all active suppliers for a tenant
     */
    @Transactional(readOnly = true)
    public List<Supplier> getActiveSuppliersByTenant(Long tenantId) {
        return supplierRepository.findByTenant_IdAndStatus(tenantId, Supplier.SupplierStatus.ACTIVE);
    }

    /**
     * Search suppliers by name or code
     */
    @Transactional(readOnly = true)
    public Page<Supplier> searchSuppliers(Long tenantId, String searchTerm, Pageable pageable) {
        return supplierRepository.searchByNameOrCode(tenantId, searchTerm, pageable);
    }

    /**
     * Delete supplier
     */
    public void deleteSupplier(Long supplierId, Long tenantId, UserAccount currentUser) {
        Supplier supplier = getSupplierByIdAndTenant(supplierId, tenantId);
        
        validateSupplierForDeletion(supplier);
        
        supplierRepository.delete(supplier);
        
    }

    /**
     * Check if supplier code exists
     */
    @Transactional(readOnly = true)
    public boolean supplierCodeExists(String code, Long tenantId) {
        return supplierRepository.existsByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Check if supplier code exists excluding specific ID
     */
    @Transactional(readOnly = true)
    public boolean supplierCodeExistsExcludingId(String code, Long tenantId, Long excludeId) {
        return supplierRepository.existsByTenant_IdAndCodeAndIdNot(tenantId, code, excludeId);
    }

    /**
     * Count suppliers by tenant
     */
    @Transactional(readOnly = true)
    public long countSuppliersByTenant(Long tenantId) {
        return supplierRepository.count();
    }

    /**
     * Activate supplier
     */
    public Supplier activateSupplier(Long supplierId, Long tenantId, UserAccount currentUser) {
        Supplier supplier = getSupplierByIdAndTenant(supplierId, tenantId);
        supplier.setStatus(Supplier.SupplierStatus.ACTIVE);
        
        Supplier savedSupplier = supplierRepository.save(supplier);
        
        
        return savedSupplier;
    }

    /**
     * Deactivate supplier
     */
    public Supplier deactivateSupplier(Long supplierId, Long tenantId, UserAccount currentUser) {
        Supplier supplier = getSupplierByIdAndTenant(supplierId, tenantId);
        supplier.setStatus(Supplier.SupplierStatus.INACTIVE);
        
        Supplier savedSupplier = supplierRepository.save(supplier);
        
        
        return savedSupplier;
    }

    // Validation methods

    private void validateSupplierForCreation(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
        
        if (supplier.getTenant() == null) {
            throw new IllegalArgumentException("Tenant is required");
        }
        
        if (supplier.getCode() == null || supplier.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier code is required");
        }
        
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required");
        }
        
        // Check for duplicate code
        if (supplierCodeExists(supplier.getCode(), supplier.getTenant().getId())) {
            throw new IllegalArgumentException("Supplier code already exists: " + supplier.getCode());
        }
        
        // Validate email format if provided
        if (supplier.getContactEmail() != null && !supplier.getContactEmail().trim().isEmpty()) {
            validateEmailFormat(supplier.getContactEmail());
        }
    }

    private void validateSupplierForUpdate(Supplier supplierDetails, Supplier existingSupplier) {
        if (supplierDetails == null) {
            throw new IllegalArgumentException("Supplier details cannot be null");
        }
        
        if (supplierDetails.getCode() == null || supplierDetails.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier code is required");
        }
        
        if (supplierDetails.getName() == null || supplierDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required");
        }
        
        // Check for duplicate code excluding current supplier
        if (supplierCodeExistsExcludingId(supplierDetails.getCode(), 
                                        existingSupplier.getTenant().getId(), 
                                        existingSupplier.getId())) {
            throw new IllegalArgumentException("Supplier code already exists: " + supplierDetails.getCode());
        }
        
        // Validate email format if provided
        if (supplierDetails.getContactEmail() != null && !supplierDetails.getContactEmail().trim().isEmpty()) {
            validateEmailFormat(supplierDetails.getContactEmail());
        }
    }

    private void validateSupplierForDeletion(Supplier supplier) {
        // Check if supplier has associated items
        // This would need to be implemented based on the relationship structure
        // For now, we'll just check if supplier is active
        if (supplier.getStatus() == Supplier.SupplierStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete active supplier. Please deactivate first.");
        }
    }

    private void validateEmailFormat(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }
}