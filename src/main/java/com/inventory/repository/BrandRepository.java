package com.inventory.repository;

import com.inventory.entity.Brand;
import com.inventory.entity.Brand.BrandStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Find all brands for a tenant with pagination
     */
    Page<Brand> findByTenant_Id(Long tenantId, Pageable pageable);

    /**
     * Find all active brands for a tenant
     */
    List<Brand> findByTenant_IdAndStatus(Long tenantId, BrandStatus status);

    /**
     * Find brand by tenant and code
     */
    Optional<Brand> findByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Find brand by tenant and id
     */
    Optional<Brand> findByTenant_IdAndId(Long tenantId, Long id);

    /**
     * Check if brand code exists for tenant
     */
    boolean existsByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Check if brand code exists for tenant excluding specific id
     */
    boolean existsByTenant_IdAndCodeAndIdNot(Long tenantId, String code, Long id);

    /**
     * Find brands by status
     */
    Page<Brand> findByTenant_IdAndStatus(Long tenantId, BrandStatus status, Pageable pageable);

    /**
     * Search brands by name or code
     */
    @Query("SELECT b FROM Brand b WHERE b.tenant.id = :tenantId " +
           "AND (UPPER(b.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(b.code) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    Page<Brand> searchByNameOrCode(@Param("tenantId") Long tenantId, 
                                 @Param("searchTerm") String searchTerm, 
                                 Pageable pageable);

    /**
     * Find brands by vendor
     */
    List<Brand> findByVendor_IdAndStatus(Long vendorId, BrandStatus status);

    /**
     * Count brands by status for a tenant
     */
    long countByTenant_IdAndStatus(Long tenantId, BrandStatus status);

    /**
     * Find brands with items count
     */
    @Query("SELECT b, COUNT(i) FROM Brand b LEFT JOIN b.items i " +
           "WHERE b.tenant.id = :tenantId AND b.status = :status " +
           "GROUP BY b ORDER BY b.name")
    List<Object[]> findBrandsWithItemCount(@Param("tenantId") Long tenantId, 
                                         @Param("status") BrandStatus status);

    /**
     * Find most popular brands by item count
     */
    @Query("SELECT b FROM Brand b JOIN b.items i " +
           "WHERE b.tenant.id = :tenantId AND b.status = 'ACTIVE' AND i.status = 'ACTIVE' " +
           "GROUP BY b ORDER BY COUNT(i) DESC")
    List<Brand> findMostPopularBrands(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Find brands without vendor
     */
    List<Brand> findByTenant_IdAndVendorIsNullAndStatus(Long tenantId, BrandStatus status);
}