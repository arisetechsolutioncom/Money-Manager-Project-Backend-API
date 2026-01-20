package com.money.money_manager.service;

import com.money.money_manager.entity.FinancialGoal;
import com.money.money_manager.entity.User;
import com.money.money_manager.dto.FinancialGoalDTO;
import com.money.money_manager.repository.FinancialGoalRepository;
import com.money.money_manager.repository.UserRepository;
import com.money.money_manager.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinancialGoalService {
    
    @Autowired
    private FinancialGoalRepository financialGoalRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Get all goals for a user
     */
    public List<FinancialGoalDTO> getAllGoals(Long userId) {
        return financialGoalRepository.findByUserId(userId).stream()
                .map(FinancialGoalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get active goals for a user
     */
    public List<FinancialGoalDTO> getActiveGoals(Long userId) {
        return financialGoalRepository.findActiveByUserId(userId).stream()
                .map(FinancialGoalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get completed goals for a user
     */
    public List<FinancialGoalDTO> getCompletedGoals(Long userId) {
        return financialGoalRepository.findCompletedByUserId(userId).stream()
                .map(FinancialGoalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific goal by ID and ensure it belongs to the user
     */
    public FinancialGoalDTO getGoalById(Long id, Long userId) {
        FinancialGoal goal = financialGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        return FinancialGoalDTO.fromEntity(goal);
    }

    /**
     * Create a new financial goal
     */
    public FinancialGoalDTO createGoal(FinancialGoalDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        FinancialGoal goal = FinancialGoal.builder()
                .user(user)
                .goalName(dto.getGoalName())
                .targetAmount(dto.getTargetAmount())
                .currentAmount(dto.getCurrentAmount() != null ? dto.getCurrentAmount() : BigDecimal.ZERO)
                .deadline(dto.getDeadline())
                .status(FinancialGoal.GoalStatus.ACTIVE)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        
        FinancialGoal savedGoal = financialGoalRepository.save(goal);
        return FinancialGoalDTO.fromEntity(savedGoal);
    }

    /**
     * Update a financial goal
     */
    public FinancialGoalDTO updateGoal(Long id, FinancialGoalDTO dto, Long userId) {
        FinancialGoal goal = financialGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        
        goal.setGoalName(dto.getGoalName());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setDeadline(dto.getDeadline());
        if (dto.getStatus() != null) {
            goal.setStatus(FinancialGoal.GoalStatus.valueOf(dto.getStatus()));
        }
        goal.setUpdatedAt(LocalDate.now());
        
        FinancialGoal updatedGoal = financialGoalRepository.save(goal);
        return FinancialGoalDTO.fromEntity(updatedGoal);
    }

    /**
     * Delete a financial goal
     */
    public void deleteGoal(Long id, Long userId) {
        FinancialGoal goal = financialGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        financialGoalRepository.delete(goal);
    }

    /**
     * Update goal progress when transaction is added
     * Called from TransactionService when a transaction is created
     */
    public void updateGoalProgress(Long userId, String transactionType, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Only update for INCOME or SAVING type transactions
        if (!("INCOME".equals(transactionType) || "SAVING".equals(transactionType))) {
            return;
        }

        // Get all incomplete goals for the user
        List<FinancialGoal> incompleteGoals = financialGoalRepository.findIncompleteByUserId(userId);
        
        for (FinancialGoal goal : incompleteGoals) {
            goal.addAmount(amount);
            // Goal's status is updated in addAmount method if target is met
            financialGoalRepository.save(goal);
        }
    }

    /**
     * Mark a goal as completed manually
     */
    public FinancialGoalDTO completeGoal(Long id, Long userId) {
        FinancialGoal goal = financialGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        
        goal.setStatus(FinancialGoal.GoalStatus.COMPLETED);
        goal.setCompletedAt(LocalDate.now());
        goal.setUpdatedAt(LocalDate.now());
        
        // Send notification
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        notificationService.notifyGoalCompleted(user, goal.getGoalName());
        
        FinancialGoal updatedGoal = financialGoalRepository.save(goal);
        return FinancialGoalDTO.fromEntity(updatedGoal);
    }

    /**
     * Cancel a goal
     */
    public FinancialGoalDTO cancelGoal(Long id, Long userId) {
        FinancialGoal goal = financialGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        
        goal.setStatus(FinancialGoal.GoalStatus.CANCELLED);
        goal.setUpdatedAt(LocalDate.now());
        
        FinancialGoal updatedGoal = financialGoalRepository.save(goal);
        return FinancialGoalDTO.fromEntity(updatedGoal);
    }

    /**
     * Get goal statistics for a user
     */
    public GoalStatisticsDTO getGoalStatistics(Long userId) {
        List<FinancialGoal> allGoals = financialGoalRepository.findByUserId(userId);
        
        long totalGoals = allGoals.size();
        long completedGoals = allGoals.stream()
                .filter(g -> FinancialGoal.GoalStatus.COMPLETED.equals(g.getStatus()))
                .count();
        long activeGoals = allGoals.stream()
                .filter(g -> FinancialGoal.GoalStatus.ACTIVE.equals(g.getStatus()))
                .count();
        
        BigDecimal totalTargetAmount = allGoals.stream()
                .map(FinancialGoal::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCurrentAmount = allGoals.stream()
                .map(FinancialGoal::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double overallProgress = totalTargetAmount.compareTo(BigDecimal.ZERO) > 0
                ? (totalCurrentAmount.doubleValue() / totalTargetAmount.doubleValue()) * 100
                : 0;
        
        return GoalStatisticsDTO.builder()
                .totalGoals(totalGoals)
                .completedGoals(completedGoals)
                .activeGoals(activeGoals)
                .totalTargetAmount(totalTargetAmount)
                .totalCurrentAmount(totalCurrentAmount)
                .overallProgress(overallProgress)
                .build();
    }

    /**
     * DTO for goal statistics
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class GoalStatisticsDTO {
        private long totalGoals;
        private long completedGoals;
        private long activeGoals;
        private BigDecimal totalTargetAmount;
        private BigDecimal totalCurrentAmount;
        private double overallProgress;
    }
}
