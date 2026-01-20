package com.money.money_manager.repository;

import com.money.money_manager.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserId(Long userId);

    Optional<RecurringTransaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.user.id = :userId AND rt.status = 'ACTIVE'")
    List<RecurringTransaction> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.user.id = :userId AND rt.status = 'PAUSED'")
    List<RecurringTransaction> findPausedByUserId(@Param("userId") Long userId);

    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.status = 'ACTIVE' AND rt.nextExecutionDate <= CURRENT_DATE")
    List<RecurringTransaction> findDueRecurringTransactions();

    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.user.id = :userId AND rt.nextExecutionDate <= :date AND rt.status = 'ACTIVE'")
    List<RecurringTransaction> findDueForUser(@Param("userId") Long userId, @Param("date") LocalDate date);
}
