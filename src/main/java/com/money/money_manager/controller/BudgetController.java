package com.money.money_manager.controller;

import com.money.money_manager.dto.ApiResponse;
import com.money.money_manager.dto.BudgetDTO;
import com.money.money_manager.dto.BudgetSummaryDTO;
import com.money.money_manager.entity.User;
import com.money.money_manager.service.BudgetService;
import com.money.money_manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for budget management.
 * All operations are user-scoped for security.
 * 
 * Sample API Requests/Responses:
 * 
 * POST /api/budgets
 * Request: {
 *   "name": "Groceries Budget",
 *   "description": "Monthly grocery spending limit",
 *   "categoryId": 1,
 *   "limitAmount": 5000.00,
 *   "startDate": "2024-01-01",
 *   "endDate": "2024-01-31",
 *   "thresholdPercent": 80
 * }
 * 
 * GET /api/budgets?page=0&size=10&status=ACTIVE&activeOnly=true
 * Response: {
 *   "success": true,
 *   "message": "Budgets retrieved successfully",
 *   "data": {
 *     "content": [
 *       {
 *         "id": 1,
 *         "name": "Groceries Budget",
 *         "limitAmount": 5000.00,
 *         "spentAmount": 3200.00,
 *         "percentUsed": 64.00,
 *         "status": "ACTIVE",
 *         "isThresholdExceeded": false
 *       }
 *     ],
 *     "totalPages": 1,
 *     "totalElements": 1
 *   }
 * }
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;
    private final UserService userService;

    /**
     * Get user budgets with optional filters.
     * 
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @param status Filter by status (ACTIVE, EXCEEDED, COMPLETED, PAUSED)
     * @param activeOnly Show only active/exceeded budgets (default false)
     * @param authentication Current user authentication
     * @return Paginated list of budgets
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BudgetSummaryDTO>>> getBudgets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> status,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            Authentication authentication) {
        
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<BudgetSummaryDTO> budgets = budgetService.getUserBudgets(
                    currentUser, status, activeOnly, pageable);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Budgets retrieved successfully", budgets));
            
        } catch (Exception e) {
            log.error("Error retrieving budgets: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get active budgets for user.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<BudgetSummaryDTO>>> getActiveBudgets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<BudgetSummaryDTO> budgets = budgetService.getUserBudgets(
                    currentUser, null, true, pageable);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Active budgets retrieved successfully", budgets));
            
        } catch (Exception e) {
            log.error("Error retrieving active budgets: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get exceeded budgets for user.
     */
    @GetMapping("/exceeded")
    public ResponseEntity<ApiResponse<List<BudgetSummaryDTO>>> getExceededBudgets(
            Authentication authentication) {
        
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            List<BudgetSummaryDTO> budgets = budgetService.getExceededBudgets(currentUser);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Exceeded budgets retrieved successfully", budgets));
            
        } catch (Exception e) {
            log.error("Error retrieving exceeded budgets: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Create new budget.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BudgetSummaryDTO>> createBudget(
            @Valid @RequestBody BudgetDTO budgetDTO,
            Authentication authentication) {
        
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            BudgetSummaryDTO budget = budgetService.createBudget(currentUser, budgetDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Budget created successfully", budget));
            
        } catch (Exception e) {
            log.error("Error creating budget: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Update existing budget.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetSummaryDTO>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDTO budgetDTO,
            Authentication authentication) {
        
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            BudgetSummaryDTO budget = budgetService.updateBudget(id, currentUser, budgetDTO);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Budget updated successfully", budget));
            
        } catch (Exception e) {
            log.error("Error updating budget {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Delete budget.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            budgetService.deleteBudget(id, currentUser);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Budget deleted successfully", null));
            
        } catch (Exception e) {
            log.error("Error deleting budget {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Manually recalculate budget.
     */
    @PostMapping("/{id}/recalc")
    public ResponseEntity<ApiResponse<BudgetSummaryDTO>> recalculateBudget(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            BudgetSummaryDTO budget = budgetService.recalculateBudgetManually(id, currentUser);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Budget recalculated successfully", budget));
            
        } catch (Exception e) {
            log.error("Error recalculating budget {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}