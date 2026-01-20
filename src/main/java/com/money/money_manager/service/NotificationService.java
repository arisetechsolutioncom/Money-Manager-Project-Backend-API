package com.money.money_manager.service;

import com.money.money_manager.dto.NotificationDTO;
import com.money.money_manager.entity.Notification;
import com.money.money_manager.entity.User;
import com.money.money_manager.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(User user, String message, Notification.NotificationType type) {
        Notification notification = new Notification(user, message, type);
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getAllNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }

    public Long getUnreadCount(User user) {
        return notificationRepository.countUnreadByUser(user);
    }

    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to notification");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    // Helper methods for specific notification types
    public void notifyBudgetExceeded(User user, String categoryName, Double amount, Double budgetLimit) {
        String message = String.format("Budget exceeded for %s! Spent ₹%.2f out of ₹%.2f limit.", 
                categoryName, amount, budgetLimit);
        createNotification(user, message, Notification.NotificationType.BUDGET_EXCEEDED);
    }

    public void notifyGoalCompleted(User user, String goalName) {
        String message = String.format("Congratulations! You've completed your financial goal: %s", goalName);
        createNotification(user, message, Notification.NotificationType.GOAL_COMPLETED);
    }

    public void notifyRecurringPaymentExecuted(User user, String description, Double amount) {
        String message = String.format("Recurring payment executed: %s - ₹%.2f", description, amount);
        createNotification(user, message, Notification.NotificationType.RECURRING_PAYMENT_EXECUTED);
    }
}