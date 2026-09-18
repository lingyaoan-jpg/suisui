package com.suisui.server.service;

import com.suisui.server.dto.WorldData;
import com.suisui.server.entity.Blocklist;
import com.suisui.server.entity.Note;
import com.suisui.server.entity.Subscription;
import com.suisui.server.entity.User;
import com.suisui.server.exception.BusinessException;
import com.suisui.server.repository.BlocklistRepository;
import com.suisui.server.repository.NoteRepository;
import com.suisui.server.repository.SubscriptionRepository;
import com.suisui.server.repository.UserRepository;
import com.suisui.server.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlocklistService {

    private final BlocklistRepository blocklistRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BlocklistService(BlocklistRepository blocklistRepository,
                            SubscriptionRepository subscriptionRepository,
                            UserRepository userRepository,
                            NoteRepository noteRepository) {
        this.blocklistRepository = blocklistRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
    }

    public List<WorldData> getBlocklist() {
        Long userId = SecurityContext.getCurrentUserId();
        List<Blocklist> blocks = blocklistRepository.findByBlockerId(userId);

        return blocks.stream().map(block -> {
            User user = userRepository.findById(block.getBlockedUserId()).orElse(null);
            if (user == null) return null;
            return buildWorldData(user);
        }).filter(w -> w != null).collect(Collectors.toList());
    }

    private WorldData buildWorldData(User user) {
        long momentCount = noteRepository.countActiveByUserId(user.getId());
        long subscriberCount = subscriptionRepository.countByTargetUserId(user.getId());

        String lastActiveTime = null;
        String latestMomentPreview = null;
        Optional<Note> latestNote = noteRepository.findFirstByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId());
        if (latestNote.isPresent()) {
            Note note = latestNote.get();
            lastActiveTime = note.getCreatedAt() != null ? note.getCreatedAt().format(FMT) : null;
            String content = note.getContent();
            latestMomentPreview = content != null && content.length() > 50
                    ? content.substring(0, 50) + "..."
                    : content;
        }

        return WorldData.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .bgUrl(user.getBgUrl())
                .bio(user.getBio())
                .worldPublic(user.getIsPublic())
                .lastActiveTime(lastActiveTime)
                .subscriberCount((int) subscriberCount)
                .momentCount((int) momentCount)
                .subscribed(false)
                .latestMomentPreview(latestMomentPreview)
                .build();
    }

    @Transactional
    public Map<String, Object> toggleBlock(long targetUserId) {
        Long userId = SecurityContext.getCurrentUserId();

        if (userId == targetUserId) {
            throw new BusinessException("不能拉黑自己");
        }

        Optional<Blocklist> existing = blocklistRepository
                .findByBlockerIdAndBlockedUserId(userId, targetUserId);

        boolean nowBlocked;
        if (existing.isPresent()) {
            blocklistRepository.delete(existing.get());
            nowBlocked = false;
        } else {
            Blocklist block = Blocklist.builder()
                    .blockerId(userId)
                    .blockedUserId(targetUserId)
                    .build();
            blocklistRepository.save(block);

            // 同时取消订阅
            Optional<Subscription> sub = subscriptionRepository
                    .findBySubscriberIdAndTargetUserId(userId, targetUserId);
            sub.ifPresent(subscriptionRepository::delete);

            nowBlocked = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("blocked", nowBlocked);
        return result;
    }
}
