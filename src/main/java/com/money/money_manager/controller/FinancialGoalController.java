package com.money.money_manager.controller;

import com.money.money_manager.dto.FinancialGoalDTO;
import com.money.money_manager.service.FinancialGoalService;
import com.money.money_manager.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class FinancialGoalController {
    
    @Autowired
    private FinancialGoalService financialGoalService;

    /**
     * Get all goals for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<FinancialGoalDTO>> getAllGoals(
            @RequestHeader("userId") Long userId) {
        List<FinancialGoalDTO> goals = financialGoalService.getAllGoals(userId);
        return ResponseEntity.ok(goals);
    }

    /**
     * Get active goals only
     */
    @GetMapping("/active")
    public ResponseEntity<List<FinancialGoalDTO>> getActiveGoals(
            @RequestHeader("userId") Long userId) {
        List<FinancialGoalDTO> goals = financialGoalService.getActiveGoals(userId);
        return ResponseEntity.ok(goals);
    }

    /**
     * Get completed goals only
     */
    @GetMapping("/completed")
    public ResponseEntity<List<FinancialGoalDTO>> getCompletedGoals(
            @RequestHeader("userId") Long userId) {
        List<FinancialGoalDTO> goals = financialGoalService.getCompletedGoals(userId);
        return ResponseEntity.ok(goals);
    }

    /**
     * Get a specific goal by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FinancialGoalDTO> getGoalById(
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {
        FinancialGoalDTO goal = financialGoalService.getGoalById(id, userId);
        return ResponseEntity.ok(goal);
    }

    /**
     * Create a new financial goal
     */
    @PostMapping
    public ResponseEntity<FinancialGoalDTO> createGoal(
            @Valid @RequestBody FinancialGoalDTO dto,
            @RequestHeader("userId") Long userId) {
        FinancialGoalDTO createdGoal = financialGoalService.createGoal(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal);
    }

    /**
     * Update a financial goal
     */
    @PutMapping("/{id}")
    public ResponseEntity<FinancialGoalDTO> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody FinancialGoalDTO dto,
            @RequestHeader("userId") Long userId) {
        FinancialGoalDTO updatedGoal = financialGoalService.updateGoal(id, dto, userId);
        return ResponseEntity.ok(updatedGoal);
    }

    /**
     * Delete a financial goal
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {
        financialGoalService.deleteGoal(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mark a goal as completed
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<FinancialGoalDTO> completeGoal(
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {
        FinancialGoalDTO completedGoal = financialGoalService.completeGoal(id, userId);
        return ResponseEntity.ok(completedGoal);
    }

    /**
     * Cancel a goal
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<FinancialGoalDTO> cancelGoal(
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {
        FinancialGoalDTO cancelledGoal = financialGoalService.cancelGoal(id, userId);
        return ResponseEntity.ok(cancelledGoal);
    }

    /**
     * Get goal statistics
     */
    @GetMapping("/statistics/summary")
    public ResponseEntity<FinancialGoalService.GoalStatisticsDTO> getGoalStatistics(
            @RequestHeader("userId") Long userId) {
        FinancialGoalService.GoalStatisticsDTO statistics = financialGoalService.getGoalStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
}
