package com.money.money_manager.controller;

import com.money.money_manager.dto.ApiResponse;
import com.money.money_manager.dto.DashboardDTO;
import com.money.money_manager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(@RequestHeader("userId") Long userId) {
        log.info("Fetching dashboard stats for user: {}", userId);
        DashboardDTO stats = dashboardService.getDashboardStats(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard stats retrieved successfully", stats));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getDashboardSummary(@RequestHeader("userId") Long userId) {
        log.info("Fetching dashboard summary for user: {}", userId);
        DashboardDTO summary = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard summary retrieved successfully", summary));
    }
}
