package com.money.money_manager.repository;

import com.money.money_manager.entity.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {

    @Query("SELECT fg FROM FinancialGoal fg WHERE fg.user.id = :userId ORDER BY fg.deadline ASC")
    List<FinancialGoal> findByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);

    @Query("SELECT fg FROM FinancialGoal fg WHERE fg.user.id = :userId AND fg.status = 'ACTIVE' ORDER BY fg.deadline ASC")
    List<FinancialGoal> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT fg FROM FinancialGoal fg WHERE fg.user.id = :userId AND fg.status = 'COMPLETED' ORDER BY fg.completedAt DESC")
    List<FinancialGoal> findCompletedByUserId(@Param("userId") Long userId);

    @Query("SELECT fg FROM FinancialGoal fg WHERE fg.user.id = :userId AND fg.id = :id")
    Optional<FinancialGoal> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT fg FROM FinancialGoal fg WHERE fg.user.id = :userId AND fg.status = 'ACTIVE' AND fg.currentAmount < fg.targetAmount")
    List<FinancialGoal> findIncompleteByUserId(@Param("userId") Long userId);
}
