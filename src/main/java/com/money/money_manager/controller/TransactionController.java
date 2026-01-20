package com.money.money_manager.controller;

import com.money.money_manager.dto.ApiResponse;
import com.money.money_manager.dto.TransactionDTO;
import com.money.money_manager.dto.TransactionSearchDTO;
import com.money.money_manager.service.CsvService;
import com.money.money_manager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final CsvService csvService;

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @RequestHeader("userId") Long userId,
            @Valid @RequestBody TransactionDTO transactionDTO) {
        log.info("Creating transaction for user: {}", userId);
        TransactionDTO created = transactionService.createTransaction(userId, transactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Transaction created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id) {
        log.info("Fetching transaction with ID: {}", id);
        TransactionDTO transaction = transactionService.getTransactionById(userId, id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transaction retrieved successfully", transaction));
    }

    @GetMapping
    public ResponseEntity<?> getUserTransactions(@RequestHeader("userId") Long userId) {
        log.info("Fetching all transactions for user: {}", userId);
        List<TransactionDTO> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transactions retrieved successfully", transactions));
    }

    @GetMapping("/range")
    public ResponseEntity<?> getTransactionsByDateRange(
            @RequestHeader("userId") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching transactions between {} and {}", startDate, endDate);
        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transactions retrieved successfully", transactions));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO transactionDTO) {
        log.info("Updating transaction with ID: {}", id);
        TransactionDTO updated = transactionService.updateTransaction(userId, id, transactionDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transaction updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id) {
        log.info("Deleting transaction with ID: {}", id);
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transaction deleted successfully", null));
    }
    
    @PostMapping("/search")
    public ResponseEntity<?> searchTransactions(
            @RequestHeader("userId") Long userId,
            @RequestBody TransactionSearchDTO searchDTO) {
        log.info("Searching transactions for user: {}", userId);
        Page<TransactionDTO> transactions = transactionService.searchTransactions(userId, searchDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transactions retrieved successfully", transactions));
    }
    
    @GetMapping("/tags")
    public ResponseEntity<?> getUserTags(@RequestHeader("userId") Long userId) {
        log.info("Fetching tags for user: {}", userId);
        List<String> tags = transactionService.getUserTags(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tags retrieved successfully", tags));
    }
    
    @GetMapping("/export")
    public ResponseEntity<String> exportTransactions(@RequestHeader("userId") Long userId) {
        log.info("Exporting transactions for user: {}", userId);
        String csvContent = csvService.exportTransactionsToCsv(userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "transactions.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
    }
    
    @PostMapping("/import")
    public ResponseEntity<?> importTransactions(
            @RequestHeader("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        log.info("Importing transactions for user: {}", userId);
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Please select a CSV file to upload", null));
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Please upload a CSV file", null));
        }
        
        try {
            CsvService.ImportResult result = csvService.importTransactionsFromCsv(userId, file);
            return ResponseEntity.ok(new ApiResponse<>(true, "Import completed", result));
        } catch (Exception e) {
            log.error("Error importing transactions: ", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Import failed: " + e.getMessage(), null));
        }
    }
}
