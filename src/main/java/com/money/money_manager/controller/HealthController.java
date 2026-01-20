package com.money.money_manager.controller;

import com.money.money_manager.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        log.info("Health check requested");
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "Money Manager API");
        data.put("version", "1.0.0");
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Service is running", data));
    }

    @GetMapping("/info")
    public ResponseEntity<?> info() {
        log.info("Info requested");
        Map<String, String> data = new HashMap<>();
        data.put("name", "Money Manager API");
        data.put("version", "1.0.0");
        data.put("description", "Complete Money Management System");
        data.put("author", "Your Team");
        
        return ResponseEntity.ok(new ApiResponse<>(true, "API Info", data));
    }
}
