package com.suisui.server.controller;

import com.suisui.server.dto.ApiResponse;
import com.suisui.server.dto.NotificationData;
import com.suisui.server.dto.PageData;
import com.suisui.server.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageData<NotificationData>>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageData<NotificationData> result = notificationService.getNotifications(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearNotifications() {
        notificationService.clearNotifications();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
