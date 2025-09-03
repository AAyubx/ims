package com.inventory.repository;

import com.inventory.entity.UserPasswordHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPasswordHistoryRepository extends JpaRepository<UserPasswordHistory, Long> {

       // Use derived query method — Spring Data will create the correct JPQL
       List<UserPasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

       // Use Spring Data Pageable to limit results instead of JPQL LIMIT
       List<UserPasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

       void deleteByUserId(Long userId);
}