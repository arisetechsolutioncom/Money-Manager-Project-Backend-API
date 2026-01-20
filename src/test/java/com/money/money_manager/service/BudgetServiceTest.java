package com.money.money_manager.service;

import com.money.money_manager.entity.Budget;
import com.money.money_manager.entity.User;
import com.money.money_manager.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BudgetService budgetService;

    private Budget testBudget;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testBudget = Budget.builder()
                .id(1L)
                .user(testUser)
                .name("Test Budget")
                .limitAmount(new BigDecimal("1000.00"))
                .spentAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .status(Budget.BudgetStatus.ACTIVE)
                .thresholdPercent(80)
                .build();
    }

    @Test
    void testRecalculateBudget_ShouldUpdateSpentAmount() {
        // Given
        BigDecimal calculatedAmount = new BigDecimal("750.00");
        when(budgetRepository.calculateSpentAmount(any(), any(), any(), any()))
                .thenReturn(calculatedAmount);
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        // When
        budgetService.recalculateBudget(testBudget);

        // Then
        verify(budgetRepository).calculateSpentAmount(
                testBudget.getUser().getId(),
                null,
                testBudget.getStartDate(),
                testBudget.getEndDate()
        );
        verify(budgetRepository).save(testBudget);
        assertEquals(calculatedAmount, testBudget.getSpentAmount());
    }

    @Test
    void testRecalculateBudget_ShouldSetExceededStatus() {
        // Given
        BigDecimal exceededAmount = new BigDecimal("1200.00");
        when(budgetRepository.calculateSpentAmount(any(), any(), any(), any()))
                .thenReturn(exceededAmount);
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        // When
        budgetService.recalculateBudget(testBudget);

        // Then
        assertEquals(Budget.BudgetStatus.EXCEEDED, testBudget.getStatus());
        assertEquals(exceededAmount, testBudget.getSpentAmount());
    }

    @Test
    void testBudgetPercentUsedCalculation() {
        // Given
        testBudget.setLimitAmount(new BigDecimal("1000.00"));
        testBudget.setSpentAmount(new BigDecimal("750.00"));

        // When
        BigDecimal percentUsed = testBudget.getPercentUsed();

        // Then
        assertEquals(new BigDecimal("75.00"), percentUsed);
    }

    @Test
    void testBudgetThresholdExceeded() {
        // Given
        testBudget.setLimitAmount(new BigDecimal("1000.00"));
        testBudget.setSpentAmount(new BigDecimal("850.00"));
        testBudget.setThresholdPercent(80);

        // When
        boolean isExceeded = testBudget.isThresholdExceeded();

        // Then
        assertTrue(isExceeded);
    }
}