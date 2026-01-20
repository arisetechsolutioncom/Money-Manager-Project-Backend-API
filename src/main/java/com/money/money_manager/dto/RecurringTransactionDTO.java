package com.money.money_manager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransactionDTO {
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 digits and 2 decimal places")
    private BigDecimal amount;
    
    @NotNull(message = "Type is required")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE")
    private String type;
    
    @NotNull(message = "Frequency is required")
    @Pattern(regexp = "DAILY|WEEKLY|BI_WEEKLY|MONTHLY|QUARTERLY|YEARLY", message = "Invalid frequency")
    private String frequency;
    
    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;
    
    private String categoryName;
    
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;
    
    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;
    
    private LocalDate lastGeneratedDate;
    
    private LocalDate nextExecutionDate;
    
    @Pattern(regexp = "ACTIVE|PAUSED|COMPLETED|CANCELLED", message = "Invalid status")
    private String status = "ACTIVE";
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Integer transactionsGenerated;
    
    private Long daysUntilNextExecution;
}
