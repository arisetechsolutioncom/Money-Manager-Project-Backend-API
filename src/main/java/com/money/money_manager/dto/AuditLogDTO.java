package com.money.money_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for audit log responses.
 * Sanitizes sensitive data before sending to frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {

    private Long id;
    private Long actorUserId;
    private String actorUsername;
    private String action;
    private String entityType;
    private Long entityId;
    private String beforeData; // Sanitized JSON
    private String afterData;  // Sanitized JSON
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}