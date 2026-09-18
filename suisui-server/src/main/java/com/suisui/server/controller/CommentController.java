package com.suisui.server.controller;

import com.suisui.server.dto.ApiResponse;
import com.suisui.server.dto.CommentCreateRequest;
import com.suisui.server.dto.CommentData;
import com.suisui.server.dto.PageData;
import com.suisui.server.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<ApiResponse<PageData<CommentData>>> getComments(
            @PathVariable long noteId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageData<CommentData> result = commentService.getComments(noteId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentData>> createComment(@Valid @RequestBody CommentCreateRequest request) {
        CommentData result = commentService.createComment(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
