package com.money.money_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for budget summary responses to frontend.
 * Contains calculated fields like percentUsed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetSummaryDTO {

    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal percentUsed;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer thresholdPercent;
    private LocalDateTime lastAlertSentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isPeriodActive;
    private boolean isThresholdExceeded;
}