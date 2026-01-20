package com.money.money_manager.controller;

import com.money.money_manager.dto.ApiResponse;
import com.money.money_manager.dto.AuditLogDTO;
import com.money.money_manager.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller for audit log management (Admin only).
 * 
 * Sample API Response:
 * GET /api/admin/audit?userId=1&entityType=Budget&action=BUDGET_CREATE&from=2024-01-01T00:00:00&to=2024-01-31T23:59:59&page=0&size=10
 * Response: {
 *   "success": true,
 *   "message": "Audit logs retrieved successfully",
 *   "data": {
 *     "content": [
 *       {
 *         "id": 1,
 *         "actorUserId": 1,
 *         "actorUsername": "john_doe",
 *         "action": "BUDGET_CREATE",
 *         "entityType": "Budget",
 *         "entityId": 123,
 *         "beforeData": null,
 *         "afterData": "{\"id\":123,\"name\":\"Groceries Budget\",\"limitAmount\":5000.00}",
 *         "ipAddress": "192.168.1.100",
 *         "userAgent": "Mozilla/5.0...",
 *         "createdAt": "2024-01-15T10:30:00"
 *       }
 *     ],
 *     "totalPages": 1,
 *     "totalElements": 1
 *   }
 * }
 */
@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // Require admin role for all endpoints
public class AuditController {

    private final AuditService auditService;

    /**
     * Get audit logs with filters (Admin only).
     * 
     * @param userId Filter by user ID
     * @param entityType Filter by entity type (e.g., "Budget", "Transaction")
     * @param action Filter by action (e.g., "BUDGET_CREATE", "TRANSACTION_UPDATE")
     * @param from Filter by date from (inclusive)
     * @param to Filter by date to (inclusive)
     * @param page Page number (default 0)
     * @param size Page size (default 20)
     * @return Paginated audit logs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLogDTO> auditLogs = auditService.getAuditLogs(
                    userId, entityType, action, from, to, pageable);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Audit logs retrieved successfully", auditLogs));
            
        } catch (Exception e) {
            log.error("Error retrieving audit logs: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get audit logs for specific entity (Admin only).
     * 
     * @param entityType Entity type (e.g., "Budget", "Transaction")
     * @param entityId Entity ID
     * @param page Page number (default 0)
     * @param size Page size (default 20)
     * @return Paginated audit logs for the entity
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getEntityAuditLogs(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLogDTO> auditLogs = auditService.getEntityAuditLogs(entityType, entityId, pageable);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Entity audit logs retrieved successfully", auditLogs));
            
        } catch (Exception e) {
            log.error("Error retrieving entity audit logs for {}:{}: {}", entityType, entityId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}