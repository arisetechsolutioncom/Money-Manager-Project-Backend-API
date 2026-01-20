package com.money.money_manager.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private BigDecimal budgetLimit;
    private BigDecimal budgetSpent;
    private int totalTransactions;
    private int totalBudgets;
    private int activeBudgets;
    private int exceededBudgets;
    private List<TransactionDTO> recentTransactions;
    private List<BudgetDTO> upcomingBudgets;
    private Double incomePercentage;
    private Double expensePercentage;
}
