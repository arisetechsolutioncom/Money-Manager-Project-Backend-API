package com.money.money_manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for enabling AOP, scheduling, and async processing.
 * Required for budget recalculation jobs and audit logging.
 */
@Configuration
@EnableScheduling
@EnableAsync
@EnableAspectJAutoProxy
public class AppConfig {
    
    // TODO: Configure mail sender for notifications
    // TODO: Configure async executor for background tasks
    // TODO: Set up proper cron schedule for production (currently 2 AM daily)
}