package com.money.money_manager.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSearchDTO {
    private String searchTerm;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String type; // INCOME, EXPENSE
    private List<Long> categoryIds;
    private List<String> tags;
    private String paymentMethod;
    private String sortBy = "transactionDate"; // transactionDate, amount, title
    private String sortDirection = "DESC"; // ASC, DESC
    private Integer page = 0;
    private Integer size = 20;
}