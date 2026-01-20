package com.money.money_manager.service;

import com.money.money_manager.dto.DashboardDTO;
import com.money.money_manager.dto.TransactionDTO;
import com.money.money_manager.dto.BudgetDTO;
import com.money.money_manager.entity.Transaction;
import com.money.money_manager.entity.Budget;
import com.money.money_manager.entity.User;
import com.money.money_manager.exception.ResourceNotFoundException;
import com.money.money_manager.repository.TransactionRepository;
import com.money.money_manager.repository.BudgetRepository;
import com.money.money_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public DashboardDTO getDashboardStats(Long userId) {
        log.info("Fetching dashboard stats for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Transaction> userTransactions = transactionRepository.findByUserId(userId);
        List<Budget> userBudgets = budgetRepository.findByUserId(userId);

        // Calculate totals
        BigDecimal totalIncome = userTransactions.stream()
                .filter(t -> "INCOME".equals(t.getType().toString()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = userTransactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType().toString()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        BigDecimal budgetLimit = userBudgets.stream()
                .map(Budget::getLimitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal budgetSpent = userBudgets.stream()
                .map(Budget::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get active and exceeded budgets
        List<Budget> activeBudgets = userBudgets.stream()
                .filter(b -> "ACTIVE".equals(b.getStatus().toString()))
                .collect(Collectors.toList());

        List<Budget> exceededBudgets = userBudgets.stream()
                .filter(b -> "EXCEEDED".equals(b.getStatus().toString()))
                .collect(Collectors.toList());

        // Get recent transactions (last 5)
        List<TransactionDTO> recentTransactions = userTransactions.stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .limit(5)
                .map(t -> modelMapper.map(t, TransactionDTO.class))
                .collect(Collectors.toList());

        // Get upcoming budgets
        List<BudgetDTO> upcomingBudgets = userBudgets.stream()
                .filter(b -> b.getEndDate().isAfter(LocalDate.now()))
                .sorted((a, b) -> a.getEndDate().compareTo(b.getEndDate()))
                .limit(5)
                .map(b -> modelMapper.map(b, BudgetDTO.class))
                .collect(Collectors.toList());

        // Calculate percentages
        Double incomePercentage = totalIncome.signum() > 0 ? 
                (totalIncome.doubleValue() / (totalIncome.doubleValue() + totalExpense.doubleValue())) * 100 : 0;
        Double expensePercentage = totalExpense.signum() > 0 ? 
                (totalExpense.doubleValue() / (totalIncome.doubleValue() + totalExpense.doubleValue())) * 100 : 0;

        return DashboardDTO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .budgetLimit(budgetLimit)
                .budgetSpent(budgetSpent)
                .totalTransactions(userTransactions.size())
                .totalBudgets(userBudgets.size())
                .activeBudgets(activeBudgets.size())
                .exceededBudgets(exceededBudgets.size())
                .recentTransactions(recentTransactions)
                .upcomingBudgets(upcomingBudgets)
                .incomePercentage(incomePercentage)
                .expensePercentage(expensePercentage)
                .build();
    }

    public DashboardDTO getDashboardSummary(Long userId) {
        log.info("Fetching dashboard summary for user: {}", userId);
        return getDashboardStats(userId);
    }
}
