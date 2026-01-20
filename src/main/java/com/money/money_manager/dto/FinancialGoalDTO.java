package com.money.money_manager.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.money.money_manager.entity.FinancialGoal;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialGoalDTO {
    private Long id;

    @NotBlank(message = "Goal name is required")
    @Size(min = 3, max = 100, message = "Goal name must be between 3 and 100 characters")
    private String goalName;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
    private BigDecimal targetAmount;

    @DecimalMin(value = "0", message = "Current amount cannot be negative")
    private BigDecimal currentAmount;

    @NotNull(message = "Deadline is required")
    @FutureOrPresent(message = "Deadline must be in the future")
    private LocalDate deadline;

    @Pattern(regexp = "ACTIVE|COMPLETED|CANCELLED", message = "Status must be ACTIVE, COMPLETED, or CANCELLED")
    private String status;

    private LocalDate createdAt;
    private LocalDate updatedAt;
    private LocalDate completedAt;

    // Computed fields
    private BigDecimal remainingAmount;
    private Double progressPercentage;
    private Long daysRemaining;
    private Boolean isDeadlinePassed;

    public static FinancialGoalDTO fromEntity(FinancialGoal goal) {
        return FinancialGoalDTO.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .deadline(goal.getDeadline())
                .status(goal.getStatus().toString())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .completedAt(goal.getCompletedAt())
                .remainingAmount(goal.getRemainingAmount())
                .progressPercentage(goal.getProgressPercentage())
                .daysRemaining(goal.getDaysRemaining())
                .isDeadlinePassed(goal.isDeadlinePassed())
                .build();
    }

    public FinancialGoal toEntity() {
        return FinancialGoal.builder()
                .goalName(this.goalName)
                .targetAmount(this.targetAmount)
                .currentAmount(this.currentAmount != null ? this.currentAmount : BigDecimal.ZERO)
                .deadline(this.deadline)
                .status(FinancialGoal.GoalStatus.valueOf(this.status != null ? this.status : "ACTIVE"))
                .build();
    }
}
