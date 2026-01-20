package com.money.money_manager.service;

import com.money.money_manager.dto.TransactionDTO;
import com.money.money_manager.entity.Category;
import com.money.money_manager.entity.Transaction;
import com.money.money_manager.entity.User;
import com.money.money_manager.repository.CategoryRepository;
import com.money.money_manager.repository.TransactionRepository;
import com.money.money_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private static final String CSV_HEADER = "Title,Description,Amount,Type,Category,Date,PaymentMethod,Tags";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String exportTransactionsToCsv(Long userId) {
        log.info("Exporting transactions to CSV for user: {}", userId);
        
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        StringBuilder csvContent = new StringBuilder();
        csvContent.append(CSV_HEADER).append("\n");

        for (Transaction transaction : transactions) {
            csvContent.append(escapeSpecialCharacters(transaction.getTitle())).append(",")
                    .append(escapeSpecialCharacters(transaction.getDescription())).append(",")
                    .append(transaction.getAmount()).append(",")
                    .append(transaction.getType()).append(",")
                    .append(escapeSpecialCharacters(transaction.getCategory().getName())).append(",")
                    .append(transaction.getTransactionDate().format(DATE_FORMATTER)).append(",")
                    .append(escapeSpecialCharacters(transaction.getPaymentMethod())).append(",")
                    .append(escapeSpecialCharacters(transaction.getTags()))
                    .append("\n");
        }

        return csvContent.toString();
    }

    public ImportResult importTransactionsFromCsv(Long userId, MultipartFile file) {
        log.info("Importing transactions from CSV for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ImportResult result = new ImportResult();
        List<String> errors = new ArrayList<>();
        List<Transaction> validTransactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                try {
                    Transaction transaction = parseCsvLine(line, user, lineNumber);
                    if (transaction != null && !isDuplicate(transaction, userId)) {
                        validTransactions.add(transaction);
                    } else if (transaction != null) {
                        errors.add("Line " + lineNumber + ": Duplicate transaction detected");
                    }
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }

            if (validTransactions.size() > 0) {
                transactionRepository.saveAll(validTransactions);
                result.setSuccessCount(validTransactions.size());
            }

        } catch (IOException e) {
            errors.add("Failed to read CSV file: " + e.getMessage());
        }

        result.setErrors(errors);
        result.setTotalProcessed(result.getSuccessCount() + errors.size());
        
        return result;
    }

    private Transaction parseCsvLine(String line, User user, int lineNumber) {
        String[] values = parseCSVLine(line);
        
        if (values.length < 6) {
            throw new RuntimeException("Invalid CSV format - missing required fields");
        }

        String title = values[0].trim();
        String description = values.length > 1 ? values[1].trim() : "";
        String amountStr = values[2].trim();
        String typeStr = values[3].trim();
        String categoryName = values[4].trim();
        String dateStr = values[5].trim();
        String paymentMethod = values.length > 6 ? values[6].trim() : "CASH";
        String tags = values.length > 7 ? values[7].trim() : "";

        // Validate required fields
        if (title.isEmpty()) {
            throw new RuntimeException("Title is required");
        }
        if (amountStr.isEmpty()) {
            throw new RuntimeException("Amount is required");
        }
        if (typeStr.isEmpty()) {
            throw new RuntimeException("Type is required");
        }
        if (categoryName.isEmpty()) {
            throw new RuntimeException("Category is required");
        }
        if (dateStr.isEmpty()) {
            throw new RuntimeException("Date is required");
        }

        // Parse and validate amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Amount must be greater than 0");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid amount format");
        }

        // Validate type
        Transaction.TransactionType type;
        try {
            type = Transaction.TransactionType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid transaction type. Must be INCOME or EXPENSE");
        }

        // Parse date
        LocalDate transactionDate;
        try {
            transactionDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date format. Use yyyy-MM-dd");
        }

        // Find or create category
        Category category = categoryRepository.findByNameAndUserId(categoryName, user.getId())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    newCategory.setType(type == Transaction.TransactionType.INCOME ? 
                            Category.CategoryType.INCOME : Category.CategoryType.EXPENSE);
                    newCategory.setUser(user);
                    return categoryRepository.save(newCategory);
                });

        // Create transaction
        return Transaction.builder()
                .title(title)
                .description(description)
                .amount(amount)
                .type(type)
                .category(category)
                .user(user)
                .transactionDate(transactionDate)
                .paymentMethod(paymentMethod)
                .tags(tags)
                .build();
    }

    private boolean isDuplicate(Transaction transaction, Long userId) {
        List<Transaction> existing = transactionRepository.findByUserIdAndTransactionDate(
                userId, transaction.getTransactionDate());
        
        return existing.stream().anyMatch(t -> 
                t.getTitle().equals(transaction.getTitle()) &&
                t.getAmount().equals(transaction.getAmount()) &&
                t.getType().equals(transaction.getType()) &&
                t.getCategory().getId().equals(transaction.getCategory().getId())
        );
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        result.add(currentField.toString());
        return result.toArray(new String[0]);
    }

    public static class ImportResult {
        private int totalProcessed;
        private int successCount;
        private List<String> errors = new ArrayList<>();

        // Getters and setters
        public int getTotalProcessed() { return totalProcessed; }
        public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }

        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }

        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}