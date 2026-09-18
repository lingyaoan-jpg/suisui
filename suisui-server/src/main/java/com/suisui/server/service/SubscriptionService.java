package com.suisui.server.service;

import com.suisui.server.dto.WorldData;
import com.suisui.server.entity.Subscription;
import com.suisui.server.entity.User;
import com.suisui.server.exception.BusinessException;
import com.suisui.server.repository.NoteRepository;
import com.suisui.server.repository.SubscriptionRepository;
import com.suisui.server.repository.UserRepository;
import com.suisui.server.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suisui.server.entity.Note;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               UserRepository userRepository,
                               NoteRepository noteRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
    }

    public List<WorldData> getSubscriptions() {
        Long userId = SecurityContext.getCurrentUserId();
        List<Subscription> subs = subscriptionRepository.findBySubscriberId(userId);

        return subs.stream().map(sub -> {
            User user = userRepository.findById(sub.getTargetUserId()).orElse(null);
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
                .subscribed(true)
                .latestMomentPreview(latestMomentPreview)
                .build();
    }

    @Transactional
    public Map<String, Object> toggleSubscription(long targetUserId) {
        Long userId = SecurityContext.getCurrentUserId();

        if (userId == targetUserId) {
            throw new BusinessException("不能订阅自己");
        }

        // 不允许订阅被拉黑的用户
        // （此校验可根据需要添加）

        Optional<Subscription> existing = subscriptionRepository
                .findBySubscriberIdAndTargetUserId(userId, targetUserId);

        boolean nowSubscribed;
        if (existing.isPresent()) {
            subscriptionRepository.delete(existing.get());
            nowSubscribed = false;
        } else {
            Subscription sub = Subscription.builder()
                    .subscriberId(userId)
                    .targetUserId(targetUserId)
                    .build();
            subscriptionRepository.save(sub);
            nowSubscribed = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("subscribed", nowSubscribed);
        return result;
    }
}
