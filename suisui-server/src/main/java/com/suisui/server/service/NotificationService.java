package com.suisui.server.service;

import com.suisui.server.dto.NotificationData;
import com.suisui.server.dto.PageData;
import com.suisui.server.entity.Notification;
import com.suisui.server.entity.User;
import com.suisui.server.repository.NotificationRepository;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public PageData<NotificationData> getNotifications(int page, int size) {
        Long userId = SecurityContext.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationData> content = notifPage.getContent().stream()
                .map(this::toNotificationData)
                .collect(Collectors.toList());

        return PageData.from(notifPage, content);
    }

    @Transactional
    public void clearNotifications() {
        Long userId = SecurityContext.getCurrentUserId();
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void createNotification(Long userId, Long fromUserId, String type, Long noteId, String contentPreview) {
        Notification notification = Notification.builder()
                .userId(userId)
                .fromUserId(fromUserId)
                .type(type)
                .noteId(noteId)
                .contentPreview(contentPreview)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    private NotificationData toNotificationData(Notification n) {
        User fromUser = userRepository.findById(n.getFromUserId()).orElse(null);

        return NotificationData.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .fromUserId(n.getFromUserId())
                .type(n.getType())
                .noteId(n.getNoteId() != null ? n.getNoteId() : 0)
                .contentPreview(n.getContentPreview())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt() != null ? n.getCreatedAt().format(FMT) : null)
                .fromUsername(fromUser != null ? fromUser.getUsername() : "")
                .fromUserAvatar(fromUser != null ? fromUser.getAvatarUrl() : "")
                .build();
    }
}
