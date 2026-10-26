package com.factory.notification.controller;

import com.factory.common.response.ApiResponse;
import com.factory.common.response.PageResponse;
import com.factory.common.security.CustomUserDetails;
import com.factory.notification.dto.NotificationResponseDto;
import com.factory.notification.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponseDto>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getId();
        String officeId = userDetails.getOfficeId();
        String role = userDetails.getRole();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<NotificationResponseDto> result = notificationService.getNotificationsForUser(userId, officeId, role, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getId();
        String officeId = userDetails.getOfficeId();
        String role = userDetails.getRole();

        long count = notificationService.getUnreadCount(userId, officeId, role);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read.", null));
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getId();
        String officeId = userDetails.getOfficeId();
        String role = userDetails.getRole();

        notificationService.markAllAsRead(userId, officeId, role);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read.", null));
    }
}
