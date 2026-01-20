package com.money.money_manager.service;

import com.money.money_manager.dto.RecurringTransactionDTO;
import com.money.money_manager.entity.RecurringTransaction;
import com.money.money_manager.entity.Category;
import com.money.money_manager.entity.User;
import com.money.money_manager.entity.Transaction;
import com.money.money_manager.exception.ResourceNotFoundException;
import com.money.money_manager.repository.RecurringTransactionRepository;
import com.money.money_manager.repository.CategoryRepository;
import com.money.money_manager.repository.UserRepository;
import com.money.money_manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public RecurringTransactionDTO createRecurringTransaction(Long userId, RecurringTransactionDTO dto) {
        log.info("Creating recurring transaction for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        RecurringTransaction recurring = modelMapper.map(dto, RecurringTransaction.class);
        recurring.setUser(user);
        recurring.setCategory(category);
        recurring.setNextExecutionDate(dto.getStartDate());
        recurring.setStatus(RecurringTransaction.RecurringStatus.ACTIVE);

        RecurringTransaction saved = recurringTransactionRepository.save(recurring);
        log.info("Recurring transaction created with ID: {}", saved.getId());

        return mapToDTO(saved);
    }

    public RecurringTransactionDTO getRecurringTransactionById(Long userId, Long recurringId) {
        log.info("Fetching recurring transaction: {}", recurringId);

        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndUserId(recurringId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        return mapToDTO(recurring);
    }

    public List<RecurringTransactionDTO> getUserRecurringTransactions(Long userId) {
        log.info("Fetching all recurring transactions for user: {}", userId);

        return recurringTransactionRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<RecurringTransactionDTO> getActiveRecurringTransactions(Long userId) {
        log.info("Fetching active recurring transactions for user: {}", userId);

        return recurringTransactionRepository.findActiveByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public RecurringTransactionDTO updateRecurringTransaction(Long userId, Long recurringId, RecurringTransactionDTO dto) {
        log.info("Updating recurring transaction: {}", recurringId);

        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndUserId(recurringId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        if (dto.getTitle() != null) {
            recurring.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            recurring.setDescription(dto.getDescription());
        }
        if (dto.getAmount() != null) {
            recurring.setAmount(dto.getAmount());
        }
        if (dto.getEndDate() != null) {
            recurring.setEndDate(dto.getEndDate());
        }

        RecurringTransaction updated = recurringTransactionRepository.save(recurring);
        return mapToDTO(updated);
    }

    public void deleteRecurringTransaction(Long userId, Long recurringId) {
        log.info("Deleting recurring transaction: {}", recurringId);

        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndUserId(recurringId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        recurringTransactionRepository.delete(recurring);
    }

    public RecurringTransactionDTO pauseRecurringTransaction(Long userId, Long recurringId) {
        log.info("Pausing recurring transaction: {}", recurringId);

        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndUserId(recurringId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        recurring.setStatus(RecurringTransaction.RecurringStatus.PAUSED);
        RecurringTransaction updated = recurringTransactionRepository.save(recurring);

        return mapToDTO(updated);
    }

    public RecurringTransactionDTO resumeRecurringTransaction(Long userId, Long recurringId) {
        log.info("Resuming recurring transaction: {}", recurringId);

        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndUserId(recurringId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        if (recurring.getStatus() == RecurringTransaction.RecurringStatus.PAUSED) {
            recurring.setStatus(RecurringTransaction.RecurringStatus.ACTIVE);
            RecurringTransaction updated = recurringTransactionRepository.save(recurring);
            return mapToDTO(updated);
        }

        return mapToDTO(recurring);
    }

    @Scheduled(cron = "0 0 1 * * *") // Run daily at 1 AM
    @Transactional
    public void processRecurringTransactions() {
        log.info("Starting recurring transaction processing");

        List<RecurringTransaction> dueTransactions = recurringTransactionRepository.findDueRecurringTransactions();

        for (RecurringTransaction recurring : dueTransactions) {
            try {
                generateTransaction(recurring);
            } catch (Exception e) {
                log.error("Error processing recurring transaction ID: {}", recurring.getId(), e);
            }
        }

        log.info("Completed recurring transaction processing");
    }

    private void generateTransaction(RecurringTransaction recurring) {
        LocalDate today = LocalDate.now();

        // Check if already generated today
        if (recurring.getLastGeneratedDate() != null && recurring.getLastGeneratedDate().isEqual(today)) {
            log.debug("Transaction already generated today for recurring ID: {}", recurring.getId());
            return;
        }

        // Create new transaction
        Transaction transaction = Transaction.builder()
                .title(recurring.getTitle())
                .description(recurring.getDescription())
                .amount(recurring.getAmount())
                .type(Transaction.TransactionType.valueOf(recurring.getType().toString()))
                .category(recurring.getCategory())
                .user(recurring.getUser())
                .transactionDate(today)
                .paymentMethod("AUTO")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("Generated transaction for recurring ID: {}", recurring.getId());

        // Update recurring transaction
        recurring.setLastGeneratedDate(today);
        recurring.setNextExecutionDate(recurring.calculateNextExecutionDate(today));

        // Check if recurring should be marked as completed
        if (recurring.getEndDate() != null && today.isAfter(recurring.getEndDate())) {
            recurring.setStatus(RecurringTransaction.RecurringStatus.COMPLETED);
        }

        recurringTransactionRepository.save(recurring);
    }

    private RecurringTransactionDTO mapToDTO(RecurringTransaction recurring) {
        RecurringTransactionDTO dto = modelMapper.map(recurring, RecurringTransactionDTO.class);
        
        if (recurring.getCategory() != null) {
            dto.setCategoryName(recurring.getCategory().getName());
        }
        
        if (recurring.getNextExecutionDate() != null) {
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), recurring.getNextExecutionDate());
            dto.setDaysUntilNextExecution(daysUntil);
        }
        
        return dto;
    }
}
