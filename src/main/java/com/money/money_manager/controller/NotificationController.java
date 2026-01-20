package com.money.money_manager.controller;

import com.money.money_manager.dto.ApiResponse;
import com.money.money_manager.dto.NotificationDTO;
import com.money.money_manager.entity.User;
import com.money.money_manager.service.NotificationService;
import com.money.money_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getAllNotifications(Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            List<NotificationDTO> notifications = notificationService.getAllNotifications(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotifications(Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "Unread notifications retrieved successfully", notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            Long count = notificationService.getUnreadCount(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "Unread count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            notificationService.markAsRead(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            notificationService.markAllAsRead(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}