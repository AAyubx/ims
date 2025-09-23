package com.inventory.service;

import com.inventory.entity.AttributeDefinition;
import com.inventory.entity.AttributeDefinition.AttributeDataType;
import com.inventory.entity.AttributeSet;
import com.inventory.entity.ItemAttributeValue;
import com.inventory.entity.UserAccount;
import com.inventory.repository.AttributeDefinitionRepository;
import com.inventory.repository.AttributeSetRepository;
import com.inventory.repository.ItemAttributeValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Attribute management
 * Handles CRUD operations and business logic for attributes
 */
@Service
@Transactional
public class AttributeService {

    @Autowired
    private AttributeDefinitionRepository attributeDefinitionRepository;

    @Autowired
    private AttributeSetRepository attributeSetRepository;

    @Autowired
    private ItemAttributeValueRepository itemAttributeValueRepository;


    /**
     * Create a new attribute definition
     */
    public AttributeDefinition createAttributeDefinition(AttributeDefinition attributeDefinition, UserAccount currentUser) {
        validateAttributeDefinitionForCreation(attributeDefinition);
        
        attributeDefinition.setCreatedBy(currentUser);
        attributeDefinition.setUpdatedBy(currentUser);
        
        AttributeDefinition savedAttributeDefinition = attributeDefinitionRepository.save(attributeDefinition);
        
        
        return savedAttributeDefinition;
    }

    /**
     * Update an existing attribute definition
     */
    public AttributeDefinition updateAttributeDefinition(Long attributeDefinitionId, AttributeDefinition attributeDefinitionDetails, UserAccount currentUser) {
        AttributeDefinition existingAttributeDefinition = getAttributeDefinitionByIdAndTenant(attributeDefinitionId, attributeDefinitionDetails.getTenant().getId());
        
        validateAttributeDefinitionForUpdate(attributeDefinitionDetails, existingAttributeDefinition);
        
        // Update fields
        existingAttributeDefinition.setCode(attributeDefinitionDetails.getCode());
        existingAttributeDefinition.setName(attributeDefinitionDetails.getName());
        existingAttributeDefinition.setDataType(attributeDefinitionDetails.getDataType());
        existingAttributeDefinition.setIsRequired(attributeDefinitionDetails.getIsRequired());
        existingAttributeDefinition.setAllowedValues(attributeDefinitionDetails.getAllowedValues());
        existingAttributeDefinition.setUpdatedBy(currentUser);
        
        AttributeDefinition savedAttributeDefinition = attributeDefinitionRepository.save(existingAttributeDefinition);
        
        
        return savedAttributeDefinition;
    }

    /**
     * Get attribute definition by ID and tenant
     */
    @Transactional(readOnly = true)
    public AttributeDefinition getAttributeDefinitionByIdAndTenant(Long attributeDefinitionId, Long tenantId) {
        return attributeDefinitionRepository.findByTenant_IdAndId(tenantId, attributeDefinitionId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute definition not found with ID: " + attributeDefinitionId));
    }

    /**
     * Get attribute definition by code and tenant
     */
    @Transactional(readOnly = true)
    public Optional<AttributeDefinition> getAttributeDefinitionByCodeAndTenant(String code, Long tenantId) {
        return attributeDefinitionRepository.findByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Get all attribute definitions for a tenant with pagination
     */
    @Transactional(readOnly = true)
    public Page<AttributeDefinition> getAttributeDefinitionsByTenant(Long tenantId, Pageable pageable) {
        return attributeDefinitionRepository.findByTenant_Id(tenantId, pageable);
    }

    /**
     * Get attribute definitions by data type
     */
    @Transactional(readOnly = true)
    public List<AttributeDefinition> getAttributeDefinitionsByDataType(Long tenantId, AttributeDataType dataType) {
        return attributeDefinitionRepository.findByTenant_IdAndDataType(tenantId, dataType);
    }

    /**
     * Search attribute definitions by name or code
     */
    @Transactional(readOnly = true)
    public Page<AttributeDefinition> searchAttributeDefinitions(Long tenantId, String searchTerm, Pageable pageable) {
        return attributeDefinitionRepository.searchByNameOrCode(tenantId, searchTerm, pageable);
    }

    /**
     * Delete attribute definition
     */
    public void deleteAttributeDefinition(Long attributeDefinitionId, Long tenantId, UserAccount currentUser) {
        AttributeDefinition attributeDefinition = getAttributeDefinitionByIdAndTenant(attributeDefinitionId, tenantId);
        
        validateAttributeDefinitionForDeletion(attributeDefinition);
        
        attributeDefinitionRepository.delete(attributeDefinition);
        
    }

    /**
     * Check if attribute definition code exists
     */
    @Transactional(readOnly = true)
    public boolean attributeDefinitionCodeExists(String code, Long tenantId) {
        return attributeDefinitionRepository.existsByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Check if attribute definition code exists excluding specific ID
     */
    @Transactional(readOnly = true)
    public boolean attributeDefinitionCodeExistsExcludingId(String code, Long tenantId, Long excludeId) {
        return attributeDefinitionRepository.existsByTenant_IdAndCodeAndIdNot(tenantId, code, excludeId);
    }

    /**
     * Count attribute definitions for a tenant
     */
    @Transactional(readOnly = true)
    public long countAttributeDefinitionsByTenant(Long tenantId) {
        // TODO: Implement when repository method is available
        return 0;
    }

    // AttributeSet methods

    /**
     * Create attribute set
     */
    public AttributeSet createAttributeSet(AttributeSet attributeSet, UserAccount currentUser) {
        validateAttributeSetForCreation(attributeSet);
        
        AttributeSet savedAttributeSet = attributeSetRepository.save(attributeSet);
        
        
        return savedAttributeSet;
    }

    /**
     * Get attribute sets by category
     */
    @Transactional(readOnly = true)
    public List<AttributeSet> getAttributeSetsByCategory(Long tenantId, Long categoryId) {
        return attributeSetRepository.findByTenant_IdAndCategory_Id(tenantId, categoryId);
    }

    /**
     * Delete attribute set
     */
    public void deleteAttributeSet(Long attributeSetId, Long tenantId, UserAccount currentUser) {
        // TODO: Implement when repository method is available
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // Validation methods

    private void validateAttributeDefinitionForCreation(AttributeDefinition attributeDefinition) {
        if (attributeDefinition == null) {
            throw new IllegalArgumentException("Attribute definition cannot be null");
        }
        
        if (attributeDefinition.getTenant() == null) {
            throw new IllegalArgumentException("Tenant is required");
        }
        
        if (attributeDefinition.getCode() == null || attributeDefinition.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute definition code is required");
        }
        
        if (attributeDefinition.getName() == null || attributeDefinition.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute definition name is required");
        }
        
        if (attributeDefinition.getDataType() == null) {
            throw new IllegalArgumentException("Data type is required");
        }
        
        // Check for duplicate code
        if (attributeDefinitionCodeExists(attributeDefinition.getCode(), attributeDefinition.getTenant().getId())) {
            throw new IllegalArgumentException("Attribute definition code already exists: " + attributeDefinition.getCode());
        }
        
        // Validate allowed values for LIST type
        if (attributeDefinition.getDataType() == AttributeDataType.LIST) {
            if (attributeDefinition.getAllowedValues() == null || attributeDefinition.getAllowedValues().trim().isEmpty()) {
                throw new IllegalArgumentException("Allowed values are required for LIST type attributes");
            }
        }
    }

    private void validateAttributeDefinitionForUpdate(AttributeDefinition attributeDefinitionDetails, AttributeDefinition existingAttributeDefinition) {
        if (attributeDefinitionDetails == null) {
            throw new IllegalArgumentException("Attribute definition details cannot be null");
        }
        
        if (attributeDefinitionDetails.getCode() == null || attributeDefinitionDetails.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute definition code is required");
        }
        
        if (attributeDefinitionDetails.getName() == null || attributeDefinitionDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute definition name is required");
        }
        
        if (attributeDefinitionDetails.getDataType() == null) {
            throw new IllegalArgumentException("Data type is required");
        }
        
        // Check for duplicate code excluding current attribute definition
        if (attributeDefinitionCodeExistsExcludingId(attributeDefinitionDetails.getCode(), 
                                                   existingAttributeDefinition.getTenant().getId(), 
                                                   existingAttributeDefinition.getId())) {
            throw new IllegalArgumentException("Attribute definition code already exists: " + attributeDefinitionDetails.getCode());
        }
        
        // Validate allowed values for LIST type
        if (attributeDefinitionDetails.getDataType() == AttributeDataType.LIST) {
            if (attributeDefinitionDetails.getAllowedValues() == null || attributeDefinitionDetails.getAllowedValues().trim().isEmpty()) {
                throw new IllegalArgumentException("Allowed values are required for LIST type attributes");
            }
        }
    }

    private void validateAttributeDefinitionForDeletion(AttributeDefinition attributeDefinition) {
        // TODO: Check if attribute definition is used in any attribute sets
        // TODO: Check if attribute definition has values assigned
        // For now, allow deletion without validation
    }

    private void validateAttributeSetForCreation(AttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new IllegalArgumentException("Attribute set cannot be null");
        }
        
        if (attributeSet.getTenant() == null) {
            throw new IllegalArgumentException("Tenant is required");
        }
        
        if (attributeSet.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        
        if (attributeSet.getAttributeDefinition() == null) {
            throw new IllegalArgumentException("Attribute definition is required");
        }
        
        // Validate that category and attribute definition belong to the same tenant
        if (!attributeSet.getCategory().getTenant().getId().equals(attributeSet.getTenant().getId())) {
            throw new IllegalArgumentException("Category must belong to the same tenant");
        }
        
        if (!attributeSet.getAttributeDefinition().getTenant().getId().equals(attributeSet.getTenant().getId())) {
            throw new IllegalArgumentException("Attribute definition must belong to the same tenant");
        }
    }
}