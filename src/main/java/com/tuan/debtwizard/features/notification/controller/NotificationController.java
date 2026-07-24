package com.tuan.debtwizard.features.notification.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.notification.dto.NotificationResponse;
import com.tuan.debtwizard.features.notification.model.Notification;
import com.tuan.debtwizard.features.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "Bearer Authentication")
@Validated
public class NotificationController {
    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(notificationService.getNotifications(userDetails));
    }
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails){
        notificationService.markAllAsRead(userDetails);
        return ApiResponse.success();
    }
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Positive Long id) {
        notificationService.markAsRead(id, userDetails);
        return ApiResponse.success();
    }
}
