package com.inventory.service;

import com.inventory.entity.UnitOfMeasure;
import com.inventory.repository.UnitOfMeasureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

/**
 * Service for Unit of Measure operations
 */
@Service
@Transactional
public class UnitOfMeasureService {

    @Autowired
    private UnitOfMeasureRepository unitOfMeasureRepository;

    /**
     * Get unit of measure by ID for tenant
     */
    public UnitOfMeasure getUnitOfMeasure(Long tenantId, Long uomId) {
        return unitOfMeasureRepository.findByTenant_IdAndId(tenantId, uomId)
                .orElseThrow(() -> new EntityNotFoundException("Unit of Measure not found with ID: " + uomId));
    }
}