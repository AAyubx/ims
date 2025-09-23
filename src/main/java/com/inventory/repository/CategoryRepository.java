package com.inventory.repository;

import com.inventory.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find all categories for a tenant with pagination
     */
    Page<Category> findByTenant_Id(Long tenantId, Pageable pageable);

    /**
     * Find all categories for a tenant
     */
    List<Category> findByTenant_Id(Long tenantId);

    /**
     * Find category by tenant and code
     */
    Optional<Category> findByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Find category by tenant and id
     */
    Optional<Category> findByTenant_IdAndId(Long tenantId, Long id);

    /**
     * Check if category code exists for tenant
     */
    boolean existsByTenant_IdAndCode(Long tenantId, String code);

    /**
     * Check if category code exists for tenant excluding specific id
     */
    boolean existsByTenant_IdAndCodeAndIdNot(Long tenantId, String code, Long id);

    /**
     * Find categories by department
     */
    List<Category> findByTenant_IdAndDepartment_Id(Long tenantId, Long departmentId);

    /**
     * Find categories by department with pagination
     */
    Page<Category> findByTenant_IdAndDepartment_Id(Long tenantId, Long departmentId, Pageable pageable);

    /**
     * Find root categories (no parent) for a department
     */
    List<Category> findByTenant_IdAndDepartment_IdAndParentIsNull(Long tenantId, Long departmentId);

    /**
     * Find child categories of a parent
     */
    List<Category> findByTenant_IdAndParent_Id(Long tenantId, Long parentId);

    /**
     * Search categories by name or code
     */
    @Query("SELECT c FROM Category c WHERE c.tenant.id = :tenantId " +
           "AND (UPPER(c.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "OR UPPER(c.code) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    Page<Category> searchByNameOrCode(@Param("tenantId") Long tenantId, 
                                    @Param("searchTerm") String searchTerm, 
                                    Pageable pageable);

    /**
     * Find categories with item count
     */
    @Query("SELECT c, COUNT(i) FROM Category c LEFT JOIN c.items i " +
           "WHERE c.tenant.id = :tenantId " +
           "GROUP BY c ORDER BY c.name")
    List<Object[]> findCategoriesWithItemCount(@Param("tenantId") Long tenantId);

    /**
     * Count categories for a tenant
     */
    long countByTenant_Id(Long tenantId);

    /**
     * Find categories by attribute set
     */
    @Query("SELECT c FROM Category c JOIN c.attributeSets ast WHERE c.tenant.id = :tenantId AND ast.id = :attributeSetId")
    List<Category> findByTenant_IdAndAttributeSet_Id(@Param("tenantId") Long tenantId, @Param("attributeSetId") Long attributeSetId);
}