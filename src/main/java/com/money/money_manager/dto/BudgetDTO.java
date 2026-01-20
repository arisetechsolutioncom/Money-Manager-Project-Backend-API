package com.money.money_manager.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating and updating budgets.
 * Uses BigDecimal for precise money handling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDTO {

    @NotBlank(message = "Budget name is required")
    @Size(max = 255, message = "Budget name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Long categoryId; // null means all categories

    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "0.01", message = "Limit amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal limitAmount;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @Min(value = 1, message = "Threshold percent must be at least 1")
    @Max(value = 100, message = "Threshold percent cannot exceed 100")
    private Integer thresholdPercent = 80;

    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let other validations handle null values
        }
        return endDate.isAfter(startDate);
    }
}