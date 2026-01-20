package com.money.money_manager.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.money.money_manager.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP Aspect for automatic audit logging.
 * Captures before/after state of entities and logs changes.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    /**
     * Around advice for methods annotated with @Auditable.
     * Captures entity state before and after method execution.
     */
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Long actorUserId = getCurrentUserId();
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        String beforeData = null;
        Object result = null;
        String afterData = null;
        Long entityId = null;

        try {
            // Capture before state if enabled
            if (auditable.captureBefore()) {
                beforeData = captureEntityState(joinPoint, auditable);
            }

            // Execute the method
            result = joinPoint.proceed();

            // Capture after state if enabled
            if (auditable.captureAfter()) {
                afterData = captureEntityState(joinPoint, auditable, result);
                entityId = extractEntityId(result);
            }

            // Log the audit event
            auditService.log(
                    actorUserId,
                    auditable.action(),
                    auditable.entityType(),
                    entityId,
                    beforeData,
                    afterData,
                    ipAddress,
                    userAgent);

        } catch (Exception e) {
            log.error("Error in audit aspect for action: {}", auditable.action(), e);
            // Still log the audit event even if there was an error
            auditService.log(
                    actorUserId,
                    auditable.action() + "_ERROR",
                    auditable.entityType(),
                    entityId,
                    beforeData,
                    "ERROR: " + e.getMessage(),
                    ipAddress,
                    userAgent);
            throw e;
        }

        return result;
    }

    private final com.money.money_manager.repository.UserRepository userRepository;

    /**
     * Get current authenticated user ID from SecurityContext.
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                String username = authentication.getName(); // In this app, username is email
                if (username != null) {
                    return userRepository.findByEmail(username)
                            .map(com.money.money_manager.entity.User::getId)
                            .orElse(null);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get current user ID: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Failed to get client IP: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get user agent from request.
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Failed to get user agent: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Capture entity state before method execution.
     */
    private String captureEntityState(ProceedingJoinPoint joinPoint, Auditable auditable) {
        try {
            // For update/delete operations, try to find entity ID in method parameters
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                // First argument is often the entity ID for update/delete operations
                if (args[0] instanceof Long) {
                    Long entityId = (Long) args[0];
                    // TODO: Implement entity retrieval based on entityType and entityId
                    // This would require a generic entity service or repository
                    return "{\"id\":" + entityId + ",\"note\":\"before_state_capture_needed\"}";
                }
            }
        } catch (Exception e) {
            log.warn("Failed to capture before state: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Capture entity state after method execution.
     */
    private String captureEntityState(ProceedingJoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            if (result != null) {
                return objectMapper.writeValueAsString(result);
            }
        } catch (Exception e) {
            log.warn("Failed to capture after state: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract entity ID from method result.
     */
    private Long extractEntityId(Object result) {
        try {
            if (result != null) {
                // Try to get ID field using reflection
                try {
                    var idField = result.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object idValue = idField.get(result);
                    if (idValue instanceof Long) {
                        return (Long) idValue;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Try getId() method
                    try {
                        var getIdMethod = result.getClass().getMethod("getId");
                        Object idValue = getIdMethod.invoke(result);
                        if (idValue instanceof Long) {
                            return (Long) idValue;
                        }
                    } catch (Exception ex) {
                        log.debug("Could not extract entity ID: {}", ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract entity ID: {}", e.getMessage());
        }
        return null;
    }
}