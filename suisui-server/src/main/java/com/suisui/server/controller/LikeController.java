package com.suisui.server.controller;

import com.suisui.server.dto.ApiResponse;
import com.suisui.server.dto.LikeRequest;
import com.suisui.server.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleLike(@RequestBody LikeRequest request) {
        Map<String, Object> result = likeService.toggleLike(request.getNoteId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
