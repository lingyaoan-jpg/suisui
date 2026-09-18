package com.suisui.server.service;

import com.suisui.server.entity.Like;
import com.suisui.server.entity.Note;
import com.suisui.server.exception.BusinessException;
import com.suisui.server.repository.LikeRepository;
import com.suisui.server.repository.NoteRepository;
import com.suisui.server.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final NoteRepository noteRepository;
    private final NotificationService notificationService;

    public LikeService(LikeRepository likeRepository,
                       NoteRepository noteRepository,
                       NotificationService notificationService) {
        this.likeRepository = likeRepository;
        this.noteRepository = noteRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Map<String, Object> toggleLike(long noteId) {
        Long userId = SecurityContext.getCurrentUserId();

        Note note = noteRepository.findByIdAndIsDeletedFalse(noteId)
                .orElseThrow(() -> new BusinessException("碎碎念不存在"));

        Optional<Like> existing = likeRepository.findByNoteIdAndUserId(noteId, userId);

        boolean nowLiked;
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            note.setLikeCount(Math.max(0, note.getLikeCount() - 1));
            nowLiked = false;
        } else {
            Like like = Like.builder()
                    .noteId(noteId)
                    .userId(userId)
                    .build();
            likeRepository.save(like);
            note.setLikeCount(note.getLikeCount() + 1);
            nowLiked = true;

            // 如果不是给自己的碎碎念点赞，发送通知
            if (!note.getUserId().equals(userId)) {
                notificationService.createNotification(
                        note.getUserId(), userId, "LIKE", noteId, null);
            }
        }

        noteRepository.save(note);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", nowLiked);
        result.put("likeCount", note.getLikeCount());
        return result;
    }
}
