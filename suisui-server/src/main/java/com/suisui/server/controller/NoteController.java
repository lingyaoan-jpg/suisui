package com.suisui.server.controller;

import com.suisui.server.dto.ApiResponse;
import com.suisui.server.dto.NoteCreateRequest;
import com.suisui.server.dto.NoteData;
import com.suisui.server.dto.PageData;
import com.suisui.server.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<PageData<NoteData>>> getUserNotes(
            @PathVariable long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tag) {
        PageData<NoteData> result = noteService.getUserNotes(userId, page, size, tag);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/detail/{noteId}")
    public ResponseEntity<ApiResponse<NoteData>> getNoteDetail(@PathVariable long noteId) {
        NoteData result = noteService.getNoteDetail(noteId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoteData>> createNote(@Valid @RequestBody NoteCreateRequest request) {
        NoteData result = noteService.createNote(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(@PathVariable long noteId) {
        noteService.deleteNote(noteId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
