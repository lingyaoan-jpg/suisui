package com.suisui.server.service;

import com.suisui.server.dto.NoteCreateRequest;
import com.suisui.server.dto.NoteData;
import com.suisui.server.dto.PageData;
import com.suisui.server.entity.*;
import com.suisui.server.exception.BusinessException;
import com.suisui.server.repository.*;
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
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteImageRepository noteImageRepository;
    private final NoteTagRepository noteTagRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NoteService(NoteRepository noteRepository,
                       NoteImageRepository noteImageRepository,
                       NoteTagRepository noteTagRepository,
                       LikeRepository likeRepository,
                       UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.noteImageRepository = noteImageRepository;
        this.noteTagRepository = noteTagRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    public PageData<NoteData> getUserNotes(long userId, int page, int size, String tag) {
        Long currentUserId = getCurrentUserIdOrNull();

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> notePage;

        if (tag != null && !tag.isEmpty()) {
            notePage = noteRepository.findByUserIdAndTag(userId, tag, pageable);
        } else {
            notePage = noteRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable);
        }

        List<NoteData> content = notePage.getContent().stream()
                .map(note -> toNoteData(note, currentUserId))
                .collect(Collectors.toList());

        return PageData.from(notePage, content);
    }

    public NoteData getNoteDetail(long noteId) {
        Long currentUserId = getCurrentUserIdOrNull();

        Note note = noteRepository.findByIdAndIsDeletedFalse(noteId)
                .orElseThrow(() -> new BusinessException("碎碎念不存在或已删除"));

        return toNoteData(note, currentUserId);
    }

    private Long getCurrentUserIdOrNull() {
        try {
            return SecurityContext.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public NoteData createNote(NoteCreateRequest request) {
        Long userId = SecurityContext.getCurrentUserId();

        Note note = Note.builder()
                .userId(userId)
                .content(request.getContent())
                .likeCount(0)
                .commentCount(0)
                .isDeleted(false)
                .build();
        note = noteRepository.save(note);

        // 保存图片
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (int i = 0; i < request.getImages().size(); i++) {
                NoteImage image = NoteImage.builder()
                        .noteId(note.getId())
                        .imageUrl(request.getImages().get(i))
                        .sortOrder(i)
                        .build();
                noteImageRepository.save(image);
            }
        }

        // 保存标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tag : request.getTags()) {
                if (tag != null && !tag.trim().isEmpty()) {
                    NoteTag noteTag = NoteTag.builder()
                            .noteId(note.getId())
                            .tagName(tag.trim())
                            .build();
                    noteTagRepository.save(noteTag);
                }
            }
        }

        return toNoteData(note, userId);
    }

    @Transactional
    public void deleteNote(long noteId) {
        Long userId = SecurityContext.getCurrentUserId();
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException("碎碎念不存在"));

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的碎碎念");
        }

        note.setIsDeleted(true);
        noteRepository.save(note);
    }

    private NoteData toNoteData(Note note, Long currentUserId) {
        List<String> imageUrls = noteImageRepository.findByNoteIdOrderBySortOrderAsc(note.getId())
                .stream().map(NoteImage::getImageUrl).collect(Collectors.toList());

        List<String> tagNames = noteTagRepository.findByNoteId(note.getId())
                .stream().map(NoteTag::getTagName).collect(Collectors.toList());

        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = likeRepository.existsByNoteIdAndUserId(note.getId(), currentUserId);
        }

        User user = userRepository.findById(note.getUserId()).orElse(null);

        NoteData data = NoteData.builder()
                .id(note.getId())
                .userId(note.getUserId())
                .content(note.getContent())
                .images(imageUrls)
                .tags(tagNames)
                .likeCount(note.getLikeCount())
                .commentCount(note.getCommentCount())
                .isLiked(isLiked)
                .createdAt(note.getCreatedAt() != null ? note.getCreatedAt().format(FMT) : null)
                .updatedAt(note.getUpdatedAt() != null ? note.getUpdatedAt().format(FMT) : null)
                .username(user != null ? user.getUsername() : "")
                .userNickname(user != null ? user.getNickname() : "")
                .userAvatar(user != null ? user.getAvatarUrl() : "")
                .build();

        return data;
    }
}
