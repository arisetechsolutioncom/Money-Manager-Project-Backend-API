package com.money.money_manager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
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
    
    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;
    
    private String categoryName;
    
    @NotNull(message = "Transaction date is required")
    private java.time.LocalDate transactionDate;
    
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod = "CASH";
    
    private String receiptUrl;
    
    private String tags;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
