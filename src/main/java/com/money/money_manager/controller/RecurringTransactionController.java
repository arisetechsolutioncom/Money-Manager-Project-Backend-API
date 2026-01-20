package com.money.money_manager.controller;

import com.money.money_manager.dto.RecurringTransactionDTO;
import com.money.money_manager.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping
    public ResponseEntity<RecurringTransactionDTO> createRecurringTransaction(
            @RequestHeader("userId") Long userId,
            @Valid @RequestBody RecurringTransactionDTO dto) {
        log.info("POST /api/recurring-transactions - Create new recurring transaction");
        RecurringTransactionDTO created = recurringTransactionService.createRecurringTransaction(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionDTO>> getAllRecurringTransactions(
            @RequestHeader("userId") Long userId) {
        log.info("GET /api/recurring-transactions - Get all recurring transactions for user");
        List<RecurringTransactionDTO> transactions = recurringTransactionService.getUserRecurringTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RecurringTransactionDTO>> getActiveRecurringTransactions(
            @RequestHeader("userId") Long userId) {
        log.info("GET /api/recurring-transactions/active - Get active recurring transactions");
        List<RecurringTransactionDTO> transactions = recurringTransactionService.getActiveRecurringTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransactionDTO> getRecurringTransactionById(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id) {
        log.info("GET /api/recurring-transactions/{} - Get recurring transaction", id);
        RecurringTransactionDTO transaction = recurringTransactionService.getRecurringTransactionById(userId, id);
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionDTO> updateRecurringTransaction(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RecurringTransactionDTO dto) {
        log.info("PUT /api/recurring-transactions/{} - Update recurring transaction", id);
        RecurringTransactionDTO updated = recurringTransactionService.updateRecurringTransaction(userId, id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringTransaction(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id) {
        log.info("DELETE /api/recurring-transactions/{} - Delete recurring transaction", id);
        recurringTransactionService.deleteRecurringTransaction(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<RecurringTransactionDTO> pauseRecurringTransaction(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id) {
        log.info("PATCH /api/recurring-transactions/{}/pause - Pause recurring transaction", id);
        RecurringTransactionDTO paused = recurringTransactionService.pauseRecurringTransaction(userId, id);
        return ResponseEntity.ok(paused);
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<RecurringTransactionDTO> resumeRecurringTransaction(
            @RequestHeader("userId") Long userId,
            @PathVariable Long id) {
        log.info("PATCH /api/recurring-transactions/{}/resume - Resume recurring transaction", id);
        RecurringTransactionDTO resumed = recurringTransactionService.resumeRecurringTransaction(userId, id);
        return ResponseEntity.ok(resumed);
    }
}
