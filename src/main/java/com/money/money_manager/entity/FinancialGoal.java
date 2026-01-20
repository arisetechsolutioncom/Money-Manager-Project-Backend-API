package com.money.money_manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "financial_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String goalName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDate updatedAt = LocalDate.now();

    @Column(name = "completed_at")
    private LocalDate completedAt;

    public enum GoalStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    /**
     * Get the remaining amount needed to reach target
     */
    public BigDecimal getRemainingAmount() {
        BigDecimal remaining = targetAmount.subtract(currentAmount);
        return remaining.max(BigDecimal.ZERO);
    }

    /**
     * Get the progress percentage (0-100)
     */
    public Double getProgressPercentage() {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return (currentAmount.doubleValue() / targetAmount.doubleValue()) * 100;
    }

    /**
     * Check if goal is completed (current amount >= target amount)
     */
    public boolean isGoalMet() {
        return currentAmount.compareTo(targetAmount) >= 0;
    }

    /**
     * Update current amount safely without exceeding target
     */
    public void addAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newAmount = currentAmount.add(amount);
            // Cap at target amount to prevent overflow
            if (newAmount.compareTo(targetAmount) > 0) {
                this.currentAmount = targetAmount;
                this.status = GoalStatus.COMPLETED;
                this.completedAt = LocalDate.now();
            } else {
                this.currentAmount = newAmount;
            }
            this.updatedAt = LocalDate.now();
        }
    }

    /**
     * Get days remaining until deadline
     */
    public long getDaysRemaining() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    /**
     * Check if deadline has passed
     */
    public boolean isDeadlinePassed() {
        return LocalDate.now().isAfter(deadline);
    }
}
