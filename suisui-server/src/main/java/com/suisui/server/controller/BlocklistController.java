package com.suisui.server.controller;

import com.suisui.server.dto.ApiResponse;
import com.suisui.server.dto.WorldData;
import com.suisui.server.service.BlocklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blocklist")
public class BlocklistController {

    private final BlocklistService blocklistService;

    public BlocklistController(BlocklistService blocklistService) {
        this.blocklistService = blocklistService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorldData>>> getBlocklist() {
        List<WorldData> list = blocklistService.getBlocklist();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleBlock(@PathVariable long targetUserId) {
        Map<String, Object> result = blocklistService.toggleBlock(targetUserId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
