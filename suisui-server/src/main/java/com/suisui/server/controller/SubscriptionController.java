package com.suisui.server.controller;

import com.suisui.server.dto.ApiResponse;
import com.suisui.server.dto.WorldData;
import com.suisui.server.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorldData>>> getSubscriptions() {
        List<WorldData> list = subscriptionService.getSubscriptions();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleSubscription(@PathVariable long targetUserId) {
        Map<String, Object> result = subscriptionService.toggleSubscription(targetUserId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
