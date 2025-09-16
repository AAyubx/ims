package com.inventory.repository;

import com.inventory.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCaseAndTenant_Id(String email, Long tenantId);

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    Optional<UserAccount> findByEmployeeCodeAndTenant_Id(String employeeCode, Long tenantId);

    Page<UserAccount> findByTenant_Id(Long tenantId, Pageable pageable);

    @Query("SELECT u FROM UserAccount u WHERE u.tenant.id = :tenantId AND " +
           "(LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.employeeCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<UserAccount> findByTenant_IdAndSearchTerm(@Param("tenantId") Long tenantId,
                                                  @Param("searchTerm") String searchTerm,
                                                  Pageable pageable);

    boolean existsByEmailIgnoreCaseAndTenant_Id(String email, Long tenantId);

    boolean existsByEmployeeCodeAndTenant_Id(String employeeCode, Long tenantId);

    @Query("SELECT u FROM UserAccount u WHERE u.passwordExpiresAt IS NOT NULL AND " +
           "u.passwordExpiresAt <= :expiryDate AND u.status = 'ACTIVE'")
    List<UserAccount> findUsersWithExpiringPasswords(@Param("expiryDate") LocalDateTime expiryDate);

    @Query("SELECT u FROM UserAccount u WHERE u.accountLockedUntil IS NOT NULL AND " +
           "u.accountLockedUntil <= :currentTime")
    List<UserAccount> findUsersWithExpiredLockouts(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(u) FROM UserAccount u JOIN u.roles r WHERE r.code = 'ADMIN' AND u.status = 'ACTIVE'")
    long countActiveAdmins();

    @Query("SELECT u FROM UserAccount u WHERE u.tenant.id = :tenantId AND u.status = :status")
    Page<UserAccount> findByTenant_IdAndStatus(@Param("tenantId") Long tenantId,
                                              @Param("status") UserAccount.UserStatus status,
                                              Pageable pageable);

    @Query("SELECT u FROM UserAccount u WHERE u.tenant.id = :tenantId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.displayName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.employeeCode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:firstName IS NULL OR :firstName = '' OR " +
           "     LOWER(u.displayName) LIKE LOWER(CONCAT('%', :firstName, '%'))) " +
           "AND (:emailAddress IS NULL OR :emailAddress = '' OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :emailAddress, '%'))) " +
           "AND (:status IS NULL OR u.status = :status)")
    Page<UserAccount> findByFilters(@Param("tenantId") Long tenantId,
                                   @Param("search") String search,
                                   @Param("firstName") String firstName,
                                   @Param("emailAddress") String emailAddress,
                                   @Param("status") UserAccount.UserStatus status,
                                   Pageable pageable);

    // Method aliases for backward compatibility
    default Optional<UserAccount> findByEmailIgnoreCaseAndTenantId(String email, Long tenantId) {
        return findByEmailIgnoreCaseAndTenant_Id(email, tenantId);
    }

    default Page<UserAccount> findByTenantId(Long tenantId, Pageable pageable) {
        return findByTenant_Id(tenantId, pageable);
    }

    default Page<UserAccount> findByTenantIdAndSearchTerm(Long tenantId, String searchTerm, Pageable pageable) {
        return findByTenant_IdAndSearchTerm(tenantId, searchTerm, pageable);
    }

    default boolean existsByEmailIgnoreCaseAndTenantId(String email, Long tenantId) {
        return existsByEmailIgnoreCaseAndTenant_Id(email, tenantId);
    }

    default boolean existsByEmployeeCodeAndTenantId(String employeeCode, Long tenantId) {
        return existsByEmployeeCodeAndTenant_Id(employeeCode, tenantId);
    }
}