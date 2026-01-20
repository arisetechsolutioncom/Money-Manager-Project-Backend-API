package com.money.money_manager.repository;

import com.money.money_manager.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

       /**
        * Find all budgets for a user.
        */
       List<Budget> findByUserId(Long userId);

       long countByUserIdAndStatus(Long userId, Budget.BudgetStatus status);

       long countByUserIdAndStatusIn(Long userId, List<Budget.BudgetStatus> statuses);

       /**
        * Find all budgets for a user with optional status filter.
        */
       Page<Budget> findByUserIdAndStatusIn(Long userId, List<Budget.BudgetStatus> statuses, Pageable pageable);

       /**
        * Find budget by ID and user ID for security.
        */
       Optional<Budget> findByIdAndUserId(Long id, Long userId);

       /**
        * Find active budgets for user in date range.
        */
       @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
                     "AND b.status IN ('ACTIVE', 'EXCEEDED') " +
                     "AND b.startDate <= :endDate AND b.endDate >= :startDate")
       List<Budget> findActiveBudgetsInDateRange(@Param("userId") Long userId,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       /**
        * Find budgets that cover a specific date and category.
        */
       @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
                     "AND b.status IN ('ACTIVE', 'EXCEEDED') " +
                     "AND b.startDate <= :date AND b.endDate >= :date " +
                     "AND (b.category.id = :categoryId OR b.category IS NULL)")
       List<Budget> findBudgetsForTransaction(@Param("userId") Long userId,
                     @Param("date") LocalDate date,
                     @Param("categoryId") Long categoryId);

       /**
        * Calculate total expense amount for user/category/date range.
        */
       @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                     "WHERE t.user.id = :userId " +
                     "AND t.type = 'EXPENSE' " +
                     "AND t.transactionDate BETWEEN :startDate AND :endDate " +
                     "AND (:categoryId IS NULL OR t.category.id = :categoryId)")
       BigDecimal calculateSpentAmount(@Param("userId") Long userId,
                     @Param("categoryId") Long categoryId,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       /**
        * Find exceeded budgets for user.
        */
       List<Budget> findByUserIdAndStatus(Long userId, Budget.BudgetStatus status);

       /**
        * Find all active budgets for recalculation.
        */
       @Query("SELECT b FROM Budget b WHERE b.status IN ('ACTIVE', 'EXCEEDED')")
       List<Budget> findAllActiveBudgets();
}