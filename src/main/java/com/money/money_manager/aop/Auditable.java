package com.money.money_manager.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automatic audit logging.
 * Use on service methods that modify entities.
 * 
 * Example: @Auditable(action = "TRANSACTION_CREATE", entityType = "Transaction")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * Action being performed (e.g., "TRANSACTION_CREATE", "BUDGET_UPDATE").
     */
    String action();
    
    /**
     * Type of entity being modified (e.g., "Transaction", "Budget").
     */
    String entityType() default "";
    
    /**
     * Whether to capture before state (default true).
     */
    boolean captureBefore() default true;
    
    /**
     * Whether to capture after state (default true).
     */
    boolean captureAfter() default true;
}