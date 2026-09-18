package com.suisui.server.controller;

import com.suisui.server.dto.ApiResponse;
import com.suisui.server.dto.StatsData;
import com.suisui.server.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<StatsData>> getStats() {
        StatsData result = statsService.getStats();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
