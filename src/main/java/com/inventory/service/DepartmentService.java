package com.inventory.service;

import com.inventory.entity.Department;
import com.inventory.entity.Department.DepartmentStatus;
import com.inventory.entity.UserAccount;
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
 * Service class for Department management
 * Handles CRUD operations and business logic for departments
 */
@Service
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;


    /**
     * Create a new department
     */
    public Department createDepartment(Department department, UserAccount currentUser) {
        validateDepartmentForCreation(department);
        
        department.setCreatedBy(currentUser);
        department.setUpdatedBy(currentUser);
        department.setStatus(DepartmentStatus.ACTIVE); // New departments are active by default
        
        Department savedDepartment = departmentRepository.save(department);
        
        
        return savedDepartment;
    }

    /**
     * Update an existing department
     */
    public Department updateDepartment(Long departmentId, Department departmentDetails, UserAccount currentUser) {
        Department existingDepartment = getDepartmentByIdAndTenant(departmentId, departmentDetails.getTenant().getId());
        
        validateDepartmentForUpdate(departmentDetails, existingDepartment);
        
        // Update fields
        existingDepartment.setCode(departmentDetails.getCode());
        existingDepartment.setName(departmentDetails.getName());
        existingDepartment.setTaxClassDefault(departmentDetails.getTaxClassDefault());
        existingDepartment.setUpdatedBy(currentUser);
        
        Department savedDepartment = departmentRepository.save(existingDepartment);
        
        
        return savedDepartment;
    }

    /**
     * Get department by ID and tenant
     */
    @Transactional(readOnly = true)
    public Department getDepartmentByIdAndTenant(Long departmentId, Long tenantId) {
        return departmentRepository.findByTenant_IdAndId(tenantId, departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + departmentId));
    }

    /**
     * Get department by code and tenant
     */
    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentByCodeAndTenant(String code, Long tenantId) {
        return departmentRepository.findByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Get all departments for a tenant with pagination
     */
    @Transactional(readOnly = true)
    public Page<Department> getDepartmentsByTenant(Long tenantId, Pageable pageable) {
        return departmentRepository.findByTenant_Id(tenantId, pageable);
    }

    /**
     * Get all active departments for a tenant
     */
    @Transactional(readOnly = true)
    public List<Department> getActiveDepartmentsByTenant(Long tenantId) {
        return departmentRepository.findByTenant_IdAndStatus(tenantId, DepartmentStatus.ACTIVE);
    }

    /**
     * Get departments by status
     */
    @Transactional(readOnly = true)
    public Page<Department> getDepartmentsByStatus(Long tenantId, DepartmentStatus status, Pageable pageable) {
        return departmentRepository.findByTenant_IdAndStatus(tenantId, status, pageable);
    }

    /**
     * Search departments by name or code
     */
    @Transactional(readOnly = true)
    public Page<Department> searchDepartments(Long tenantId, String searchTerm, Pageable pageable) {
        return departmentRepository.searchByNameOrCode(tenantId, searchTerm, pageable);
    }

    /**
     * Get departments with category count
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDepartmentsWithCategoryCount(Long tenantId, DepartmentStatus status) {
        return departmentRepository.findDepartmentsWithCategoryCount(tenantId, status);
    }

    /**
     * Delete department
     */
    public void deleteDepartment(Long departmentId, Long tenantId, UserAccount currentUser) {
        Department department = getDepartmentByIdAndTenant(departmentId, tenantId);
        
        validateDepartmentForDeletion(department);
        
        departmentRepository.delete(department);
        
    }

    /**
     * Check if department code exists
     */
    @Transactional(readOnly = true)
    public boolean departmentCodeExists(String code, Long tenantId) {
        return departmentRepository.existsByTenant_IdAndCode(tenantId, code);
    }

    /**
     * Check if department code exists excluding specific ID
     */
    @Transactional(readOnly = true)
    public boolean departmentCodeExistsExcludingId(String code, Long tenantId, Long excludeId) {
        return departmentRepository.existsByTenant_IdAndCodeAndIdNot(tenantId, code, excludeId);
    }

    /**
     * Count departments by status for a tenant
     */
    @Transactional(readOnly = true)
    public long countDepartmentsByStatus(Long tenantId, DepartmentStatus status) {
        return departmentRepository.countByTenant_IdAndStatus(tenantId, status);
    }

    /**
     * Activate department
     */
    public Department activateDepartment(Long departmentId, Long tenantId, UserAccount currentUser) {
        Department department = getDepartmentByIdAndTenant(departmentId, tenantId);
        department.setStatus(DepartmentStatus.ACTIVE);
        department.setUpdatedBy(currentUser);
        
        Department savedDepartment = departmentRepository.save(department);
        
        
        return savedDepartment;
    }

    /**
     * Deactivate department
     */
    public Department deactivateDepartment(Long departmentId, Long tenantId, UserAccount currentUser) {
        Department department = getDepartmentByIdAndTenant(departmentId, tenantId);
        department.setStatus(DepartmentStatus.INACTIVE);
        department.setUpdatedBy(currentUser);
        
        Department savedDepartment = departmentRepository.save(department);
        
        
        return savedDepartment;
    }

    // Validation methods

    private void validateDepartmentForCreation(Department department) {
        if (department == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        
        if (department.getTenant() == null) {
            throw new IllegalArgumentException("Tenant is required");
        }
        
        if (department.getCode() == null || department.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Department code is required");
        }
        
        if (department.getName() == null || department.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Department name is required");
        }
        
        // Check for duplicate code
        if (departmentCodeExists(department.getCode(), department.getTenant().getId())) {
            throw new IllegalArgumentException("Department code already exists: " + department.getCode());
        }
    }

    private void validateDepartmentForUpdate(Department departmentDetails, Department existingDepartment) {
        if (departmentDetails == null) {
            throw new IllegalArgumentException("Department details cannot be null");
        }
        
        if (departmentDetails.getCode() == null || departmentDetails.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Department code is required");
        }
        
        if (departmentDetails.getName() == null || departmentDetails.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Department name is required");
        }
        
        // Check for duplicate code excluding current department
        if (departmentCodeExistsExcludingId(departmentDetails.getCode(), 
                                          existingDepartment.getTenant().getId(), 
                                          existingDepartment.getId())) {
            throw new IllegalArgumentException("Department code already exists: " + departmentDetails.getCode());
        }
    }

    private void validateDepartmentForDeletion(Department department) {
        // Check if department has categories
        if (department.hasCategories()) {
            throw new IllegalStateException("Cannot delete department with categories. Please delete or reassign categories first.");
        }
        
        // Check if department is active
        if (department.getStatus() == DepartmentStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete active department. Please deactivate first.");
        }
    }
}