package com.inventory.repository;

import com.inventory.entity.Supplier;
import com.inventory.entity.Supplier.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * Find all suppliers for a tenant with pagination
     */
    Page<Supplier> findByTenant_Id(Long tenantId, Pageable pageable);

    /**
     * Find all active suppliers for a tenant
     */
    List<Supplier> findByTenant_IdAndStatus(Long tenantId, SupplierStatus status);

    /**
     * Find supplier by tenant and code
     */
    Optional<Supplier> findByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Find supplier by tenant and id
     */
    Optional<Supplier> findByTenant_IdAndId(Long tenantId, Long id);

    /**
     * Check if supplier code exists for tenant
     */
    boolean existsByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Check if supplier code exists for tenant excluding specific id
     */
    boolean existsByTenant_IdAndCodeAndIdNot(Long tenantId, String code, Long id);

    /**
     * Find suppliers by status
     */
    Page<Supplier> findByTenant_IdAndStatus(Long tenantId, SupplierStatus status, Pageable pageable);

    /**
     * Search suppliers by name or code
     */
    @Query("SELECT s FROM Supplier s WHERE s.tenant.id = :tenantId " +
           "AND (UPPER(s.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(s.code) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    Page<Supplier> searchByNameOrCode(@Param("tenantId") Long tenantId, 
                                    @Param("searchTerm") String searchTerm, 
                                    Pageable pageable);

    /**
     * Find suppliers by email
     */
    Optional<Supplier> findByTenant_IdAndContactEmail(Long tenantId, String contactEmail);

    /**
     * Count suppliers by status for a tenant
     */
    long countByTenant_IdAndStatus(Long tenantId, SupplierStatus status);

    /**
     * Find suppliers with brands count
     */
    @Query("SELECT s, COUNT(b) FROM Supplier s LEFT JOIN s.brands b " +
           "WHERE s.tenant.id = :tenantId AND s.status = :status " +
           "GROUP BY s ORDER BY s.name")
    List<Object[]> findSuppliersWithBrandCount(@Param("tenantId") Long tenantId, 
                                             @Param("status") SupplierStatus status);

    /**
     * Find suppliers without brands
     */
    @Query("SELECT s FROM Supplier s WHERE s.tenant.id = :tenantId " +
           "AND s.status = :status " +
           "AND NOT EXISTS (SELECT 1 FROM Brand b WHERE b.vendor = s)")
    List<Supplier> findSuppliersWithoutBrands(@Param("tenantId") Long tenantId, 
                                             @Param("status") SupplierStatus status);

    /**
     * Find suppliers with contact email
     */
    List<Supplier> findByTenant_IdAndContactEmailIsNotNullAndStatus(Long tenantId, SupplierStatus status);

    /**
     * Find suppliers without contact email
     */
    List<Supplier> findByTenant_IdAndContactEmailIsNullAndStatus(Long tenantId, SupplierStatus status);
}