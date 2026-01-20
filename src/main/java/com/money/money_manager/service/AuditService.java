package com.money.money_manager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.money.money_manager.dto.AuditLogDTO;
import com.money.money_manager.entity.AuditLog;
import com.money.money_manager.entity.User;
import com.money.money_manager.repository.AuditLogRepository;
import com.money.money_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Service for managing audit logs.
 * Provides secure logging with sensitive data masking.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Fields to mask in audit logs for security
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
            "password", "email", "phoneNumber", "ssn", "creditCard"
    );

    /**
     * Log an audit event with before/after data.
     * 
     * @param actorUserId ID of user performing action
     * @param action Action being performed
     * @param entityType Type of entity being modified
     * @param entityId ID of entity being modified
     * @param beforeJson JSON representation before change
     * @param afterJson JSON representation after change
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     */
    public void log(Long actorUserId, String action, String entityType, Long entityId,
                    String beforeJson, String afterJson, String ipAddress, String userAgent) {
        try {
            User actorUser = null;
            if (actorUserId != null) {
                actorUser = userRepository.findById(actorUserId).orElse(null);
            }

            AuditLog auditLog = AuditLog.builder()
                    .actorUser(actorUser)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .beforeData(sanitizeJson(beforeJson))
                    .afterData(sanitizeJson(afterJson))
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: action={}, entityType={}, entityId={}", action, entityType, entityId);

        } catch (Exception e) {
            log.error("Failed to create audit log: action={}, entityType={}, entityId={}", 
                     action, entityType, entityId, e);
            // Don't throw exception to avoid breaking business logic
        }
    }

    /**
     * Get audit logs with filters (admin only).
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogs(Long userId, String entityType, String action,
                                          LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findWithFilters(
                userId, entityType, action, fromDate, toDate, pageable);
        
        return auditLogs.map(this::mapToDTO);
    }

    /**
     * Get audit logs for specific entity.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getEntityAuditLogs(String entityType, Long entityId, Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                entityType, entityId, pageable);
        
        return auditLogs.map(this::mapToDTO);
    }

    /**
     * Sanitize JSON by masking sensitive fields.
     */
    private String sanitizeJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }

        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                SENSITIVE_FIELDS.forEach(field -> {
                    if (objectNode.has(field)) {
                        objectNode.put(field, "***MASKED***");
                    }
                });
                return objectMapper.writeValueAsString(objectNode);
            }
            return json;
        } catch (Exception e) {
            log.warn("Failed to sanitize JSON, returning original: {}", e.getMessage());
            return json;
        }
    }

    /**
     * Map AuditLog entity to DTO.
     */
    private AuditLogDTO mapToDTO(AuditLog auditLog) {
        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .actorUserId(auditLog.getActorUser() != null ? auditLog.getActorUser().getId() : null)
                .actorUsername(auditLog.getActorUser() != null ? auditLog.getActorUser().getUsername() : null)
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .beforeData(auditLog.getBeforeData())
                .afterData(auditLog.getAfterData())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}