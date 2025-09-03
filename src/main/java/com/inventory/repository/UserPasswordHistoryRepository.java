package com.inventory.repository;

import com.inventory.entity.UserPasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPasswordHistoryRepository extends JpaRepository<UserPasswordHistory, Long> {

    @Query("SELECT uph FROM UserPasswordHistory uph WHERE uph.user.id = :userId " +
           "ORDER BY uph.createdAt DESC")
    List<UserPasswordHistory> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT uph FROM UserPasswordHistory uph WHERE uph.user.id = :userId " +
           "ORDER BY uph.createdAt DESC LIMIT :limit")
    List<UserPasswordHistory> findRecentPasswordsByUserId(@Param("userId") Long userId,
                                                           @Param("limit") int limit);

    void deleteByUserId(Long userId);
}