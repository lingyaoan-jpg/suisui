package com.suisui.server.service;

import com.suisui.server.dto.CommentCreateRequest;
import com.suisui.server.dto.CommentData;
import com.suisui.server.dto.PageData;
import com.suisui.server.entity.Comment;
import com.suisui.server.entity.Note;
import com.suisui.server.entity.User;
import com.suisui.server.exception.BusinessException;
import com.suisui.server.repository.CommentRepository;
import com.suisui.server.repository.NoteRepository;
import com.suisui.server.repository.UserRepository;
import com.suisui.server.security.SecurityContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CommentService(CommentRepository commentRepository,
                          NoteRepository noteRepository,
                          UserRepository userRepository,
                          NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public PageData<CommentData> getComments(long noteId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByNoteIdOrderByCreatedAtAsc(noteId, pageable);

        List<CommentData> content = commentPage.getContent().stream()
                .map(this::toCommentData)
                .collect(Collectors.toList());

        return PageData.from(commentPage, content);
    }

    @Transactional
    public CommentData createComment(CommentCreateRequest request) {
        Long userId = SecurityContext.getCurrentUserId();

        Note note = noteRepository.findByIdAndIsDeletedFalse(request.getNoteId())
                .orElseThrow(() -> new BusinessException("碎碎念不存在"));

        Comment comment = Comment.builder()
                .noteId(request.getNoteId())
                .userId(userId)
                .content(request.getContent())
                .build();
        comment = commentRepository.save(comment);

        // 更新评论计数
        note.setCommentCount(note.getCommentCount() + 1);
        noteRepository.save(note);

        // 如果不是给自己的碎碎念评论，发送通知
        if (!note.getUserId().equals(userId)) {
            String preview = request.getContent();
            if (preview.length() > 100) {
                preview = preview.substring(0, 100);
            }
            notificationService.createNotification(
                    note.getUserId(), userId, "COMMENT", note.getId(), preview);
        }

        return toCommentData(comment);
    }

    private CommentData toCommentData(Comment comment) {
        User user = userRepository.findById(comment.getUserId()).orElse(null);

        return CommentData.builder()
                .id(comment.getId())
                .noteId(comment.getNoteId())
                .userId(comment.getUserId())
                .username(user != null ? user.getUsername() : "")
                .userAvatar(user != null ? user.getAvatarUrl() : "")
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().format(FMT) : null)
                .build();
    }
}
