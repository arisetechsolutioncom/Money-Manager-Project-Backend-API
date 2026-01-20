package com.money.money_manager.service;

import com.money.money_manager.aop.Auditable;
import com.money.money_manager.dto.BudgetDTO;
import com.money.money_manager.dto.BudgetSummaryDTO;
import com.money.money_manager.entity.Budget;
import com.money.money_manager.entity.Category;
import com.money.money_manager.entity.Transaction;
import com.money.money_manager.entity.User;
import com.money.money_manager.exception.ResourceNotFoundException;
import com.money.money_manager.repository.BudgetRepository;
import com.money.money_manager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Service for managing budgets with automatic recalculation and notifications.
 * Uses BigDecimal for precise money calculations.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;

    /**
     * Create a new budget for user.
     */
    @Auditable(action = "BUDGET_CREATE", entityType = "Budget")
    public BudgetSummaryDTO createBudget(User currentUser, BudgetDTO dto) {
        log.info("Creating budget for user: {}", currentUser.getId());

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        Budget budget = Budget.builder()
                .user(currentUser)
                .category(category)
                .name(dto.getName())
                .description(dto.getDescription())
                .limitAmount(dto.getLimitAmount())
                .spentAmount(BigDecimal.ZERO)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(Budget.BudgetStatus.ACTIVE)
                .thresholdPercent(dto.getThresholdPercent())
                .build();

        Budget savedBudget = budgetRepository.save(budget);
        
        // Calculate initial spent amount
        recalculateBudget(savedBudget);
        
        return mapToSummaryDTO(savedBudget);
    }

    /**
     * Update existing budget.
     */
    @Auditable(action = "BUDGET_UPDATE", entityType = "Budget")
    public BudgetSummaryDTO updateBudget(Long id, User currentUser, BudgetDTO dto) {
        log.info("Updating budget {} for user: {}", id, currentUser.getId());

        Budget budget = budgetRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        budget.setName(dto.getName());
        budget.setDescription(dto.getDescription());
        budget.setCategory(category);
        budget.setLimitAmount(dto.getLimitAmount());
        budget.setStartDate(dto.getStartDate());
        budget.setEndDate(dto.getEndDate());
        budget.setThresholdPercent(dto.getThresholdPercent());

        Budget savedBudget = budgetRepository.save(budget);
        
        // Recalculate after update
        recalculateBudget(savedBudget);
        
        return mapToSummaryDTO(savedBudget);
    }

    /**
     * Delete budget.
     */
    @Auditable(action = "BUDGET_DELETE", entityType = "Budget")
    public void deleteBudget(Long id, User currentUser) {
        log.info("Deleting budget {} for user: {}", id, currentUser.getId());

        Budget budget = budgetRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        budgetRepository.delete(budget);
    }

    /**
     * Get user budgets with filters.
     */
    @Transactional(readOnly = true)
    public Page<BudgetSummaryDTO> getUserBudgets(User currentUser, List<String> statuses, 
                                                 boolean activeOnly, Pageable pageable) {
        List<Budget.BudgetStatus> statusList;
        
        if (activeOnly) {
            statusList = Arrays.asList(Budget.BudgetStatus.ACTIVE, Budget.BudgetStatus.EXCEEDED);
        } else if (statuses != null && !statuses.isEmpty()) {
            statusList = statuses.stream()
                    .map(Budget.BudgetStatus::valueOf)
                    .toList();
        } else {
            statusList = Arrays.asList(Budget.BudgetStatus.values());
        }

        Page<Budget> budgets = budgetRepository.findByUserIdAndStatusIn(
                currentUser.getId(), statusList, pageable);
        
        return budgets.map(this::mapToSummaryDTO);
    }

    /**
     * Get exceeded budgets for user.
     */
    @Transactional(readOnly = true)
    public List<BudgetSummaryDTO> getExceededBudgets(User currentUser) {
        List<Budget> budgets = budgetRepository.findByUserIdAndStatus(
                currentUser.getId(), Budget.BudgetStatus.EXCEEDED);
        
        return budgets.stream()
                .map(this::mapToSummaryDTO)
                .toList();
    }

    /**
     * Recalculate single budget spent amount and status.
     * Uses BigDecimal for precise money calculations.
     */
    @Transactional
    public void recalculateBudget(Budget budget) {
        log.debug("Recalculating budget: {}", budget.getId());

        // Calculate spent amount from transactions
        BigDecimal spentAmount = budgetRepository.calculateSpentAmount(
                budget.getUser().getId(),
                budget.getCategory() != null ? budget.getCategory().getId() : null,
                budget.getStartDate(),
                budget.getEndDate()
        );

        budget.setSpentAmount(spentAmount);

        // Update status based on spent amount
        Budget.BudgetStatus newStatus = calculateBudgetStatus(budget);
        Budget.BudgetStatus oldStatus = budget.getStatus();
        budget.setStatus(newStatus);

        budgetRepository.save(budget);

        // Send notification if threshold exceeded and not already notified recently
        if (newStatus == Budget.BudgetStatus.EXCEEDED && 
            shouldSendThresholdAlert(budget, oldStatus)) {
            sendThresholdExceededNotification(budget);
        }

        log.debug("Budget {} recalculated: spent={}, status={}", 
                 budget.getId(), spentAmount, newStatus);
    }

    /**
     * Recalculate all active budgets (scheduled job).
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 02:00 server time
    @Transactional
    public void recalculateAllBudgets() {
        log.info("Starting scheduled budget recalculation");
        
        List<Budget> activeBudgets = budgetRepository.findAllActiveBudgets();
        
        for (Budget budget : activeBudgets) {
            try {
                recalculateBudget(budget);
            } catch (Exception e) {
                log.error("Failed to recalculate budget {}: {}", budget.getId(), e.getMessage());
            }
        }
        
        log.info("Completed scheduled budget recalculation for {} budgets", activeBudgets.size());
    }

    /**
     * Handle transaction created/updated/deleted events.
     * Updates affected budgets efficiently.
     */
    @Transactional
    public void onTransactionCreated(Transaction transaction) {
        updateAffectedBudgets(transaction);
    }

    @Transactional
    public void onTransactionUpdated(Transaction transaction) {
        updateAffectedBudgets(transaction);
    }

    @Transactional
    public void onTransactionDeleted(Transaction transaction) {
        updateAffectedBudgets(transaction);
    }

    /**
     * Manual budget recalculation endpoint.
     */
    @Auditable(action = "BUDGET_RECALCULATE", entityType = "Budget")
    public BudgetSummaryDTO recalculateBudgetManually(Long id, User currentUser) {
        Budget budget = budgetRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        recalculateBudget(budget);
        return mapToSummaryDTO(budget);
    }

    /**
     * Update budgets affected by transaction change.
     */
    private void updateAffectedBudgets(Transaction transaction) {
        if (transaction.getType() != Transaction.TransactionType.EXPENSE) {
            return; // Only expense transactions affect budgets
        }

        List<Budget> affectedBudgets = budgetRepository.findBudgetsForTransaction(
                transaction.getUser().getId(),
                transaction.getTransactionDate(),
                transaction.getCategory().getId()
        );

        for (Budget budget : affectedBudgets) {
            recalculateBudget(budget);
        }
    }

    /**
     * Calculate budget status based on spent amount and period.
     */
    private Budget.BudgetStatus calculateBudgetStatus(Budget budget) {
        if (budget.getSpentAmount().compareTo(budget.getLimitAmount()) > 0) {
            return Budget.BudgetStatus.EXCEEDED;
        }
        
        if (!budget.isPeriodActive()) {
            return Budget.BudgetStatus.COMPLETED;
        }
        
        return Budget.BudgetStatus.ACTIVE;
    }

    /**
     * Check if threshold alert should be sent.
     * Throttles alerts to avoid spam (24 hour cooldown).
     */
    private boolean shouldSendThresholdAlert(Budget budget, Budget.BudgetStatus oldStatus) {
        if (oldStatus == Budget.BudgetStatus.EXCEEDED) {
            return false; // Already exceeded, don't send again
        }

        if (budget.getLastAlertSentAt() != null) {
            LocalDateTime cooldownTime = budget.getLastAlertSentAt().plusHours(24);
            if (LocalDateTime.now().isBefore(cooldownTime)) {
                return false; // Still in cooldown period
            }
        }

        return budget.isThresholdExceeded();
    }

    /**
     * Send threshold exceeded notification.
     */
    @Async
    private void sendThresholdExceededNotification(Budget budget) {
        try {
            String message = String.format(
                "Budget '%s' has exceeded %d%% threshold. Spent: ₹%s of ₹%s limit.",
                budget.getName(),
                budget.getThresholdPercent(),
                budget.getSpentAmount(),
                budget.getLimitAmount()
            );

            notificationService.createNotification(
                budget.getUser(),
                message,
                com.money.money_manager.entity.Notification.NotificationType.BUDGET_EXCEEDED
            );

            // Update last alert sent time
            budget.setLastAlertSentAt(LocalDateTime.now());
            budgetRepository.save(budget);

            log.info("Threshold exceeded notification sent for budget: {}", budget.getId());

        } catch (Exception e) {
            log.error("Failed to send threshold notification for budget {}: {}", 
                     budget.getId(), e.getMessage());
        }
    }

    /**
     * Map Budget entity to BudgetSummaryDTO.
     */
    private BudgetSummaryDTO mapToSummaryDTO(Budget budget) {
        return BudgetSummaryDTO.builder()
                .id(budget.getId())
                .name(budget.getName())
                .description(budget.getDescription())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : "All Categories")
                .limitAmount(budget.getLimitAmount())
                .spentAmount(budget.getSpentAmount())
                .percentUsed(budget.getPercentUsed())
                .status(budget.getStatus().name())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .thresholdPercent(budget.getThresholdPercent())
                .lastAlertSentAt(budget.getLastAlertSentAt())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .isPeriodActive(budget.isPeriodActive())
                .isThresholdExceeded(budget.isThresholdExceeded())
                .build();
    }
}