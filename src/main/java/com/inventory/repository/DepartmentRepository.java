package com.inventory.repository;

import com.inventory.entity.Department;
import com.inventory.entity.Department.DepartmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Find all departments for a tenant with pagination
     */
    Page<Department> findByTenant_Id(Long tenantId, Pageable pageable);

    /**
     * Find all active departments for a tenant
     */
    List<Department> findByTenant_IdAndStatus(Long tenantId, DepartmentStatus status);

    /**
     * Find department by tenant and code
     */
    Optional<Department> findByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Find department by tenant and id
     */
    Optional<Department> findByTenant_IdAndId(Long tenantId, Long id);

    /**
     * Check if department code exists for tenant
     */
    boolean existsByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Check if department code exists for tenant excluding specific id
     */
    boolean existsByTenant_IdAndCodeAndIdNot(Long tenantId, String code, Long id);

    /**
     * Find departments by status
     */
    Page<Department> findByTenant_IdAndStatus(Long tenantId, DepartmentStatus status, Pageable pageable);

    /**
     * Search departments by name or code
     */
    @Query("SELECT d FROM Department d WHERE d.tenant.id = :tenantId " +
           "AND (UPPER(d.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(d.code) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    Page<Department> searchByNameOrCode(@Param("tenantId") Long tenantId, 
                                      @Param("searchTerm") String searchTerm, 
                                      Pageable pageable);

    /**
     * Count departments by status for a tenant
     */
    long countByTenant_IdAndStatus(Long tenantId, DepartmentStatus status);

    /**
     * Find departments with categories count
     */
    @Query("SELECT d, COUNT(c) FROM Department d LEFT JOIN d.categories c " +
           "WHERE d.tenant.id = :tenantId AND d.status = :status " +
           "GROUP BY d ORDER BY d.name")
    List<Object[]> findDepartmentsWithCategoryCount(@Param("tenantId") Long tenantId, 
                                                   @Param("status") DepartmentStatus status);

    /**
     * Find departments by tax class
     */
    List<Department> findByTenant_IdAndTaxClassDefaultAndStatus(Long tenantId, String taxClass, DepartmentStatus status);
}