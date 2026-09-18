package com.suisui.server.service;

import com.suisui.server.dto.UpdateProfileRequest;
import com.suisui.server.dto.WorldData;
import com.suisui.server.entity.Note;
import com.suisui.server.entity.User;
import com.suisui.server.exception.BusinessException;
import com.suisui.server.repository.*;
import com.suisui.server.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BlocklistRepository blocklistRepository;

    public UserService(UserRepository userRepository, NoteRepository noteRepository,
                       SubscriptionRepository subscriptionRepository,
                       BlocklistRepository blocklistRepository) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.blocklistRepository = blocklistRepository;
    }

    @Transactional
    public void updateProfile(UpdateProfileRequest request) {
        Long userId = SecurityContext.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getBgUrl() != null) user.setBgUrl(request.getBgUrl());

        userRepository.save(user);
    }

    @Transactional
    public void toggleWorldPublic() {
        Long userId = SecurityContext.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setIsPublic(!user.getIsPublic());
        userRepository.save(user);
    }

    public WorldData getWorld(long userId) {
        Long currentUserId;
        try {
            currentUserId = SecurityContext.getCurrentUserId();
        } catch (Exception e) {
            currentUserId = null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return buildWorldData(user, currentUserId, fmt);
    }

    public List<WorldData> getDiscover(int page, int size) {
        Long currentUserId = SecurityContext.getCurrentUserId();
        List<Long> blockedIds = blocklistRepository.findBlockedUserIdsByBlockerId(currentUserId);

        List<User> users = userRepository.findDiscoverUsers(currentUserId, blockedIds);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return users.stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(user -> buildWorldData(user, currentUserId, fmt))
                .toList();
    }

    private WorldData buildWorldData(User user, Long currentUserId, DateTimeFormatter fmt) {
        long momentCount = noteRepository.countActiveByUserId(user.getId());
        long subscriberCount = subscriptionRepository.countByTargetUserId(user.getId());
        boolean subscribed = currentUserId != null
                && !currentUserId.equals(user.getId())
                && subscriptionRepository.existsBySubscriberIdAndTargetUserId(currentUserId, user.getId());

        // 获取最新一条碎碎念
        String lastActiveTime = null;
        String latestMomentPreview = null;
        Optional<Note> latestNote = noteRepository.findFirstByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId());
        if (latestNote.isPresent()) {
            Note note = latestNote.get();
            lastActiveTime = note.getCreatedAt() != null ? note.getCreatedAt().format(fmt) : null;
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
                .subscribed(subscribed)
                .latestMomentPreview(latestMomentPreview)
                .build();
    }
}
