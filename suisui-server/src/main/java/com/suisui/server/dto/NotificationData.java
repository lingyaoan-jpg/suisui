package com.suisui.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationData {

    private long id;
    private long userId;
    private long fromUserId;
    private String type;
    private long noteId;
    private String contentPreview;
    private boolean isRead;
    private String createdAt;

    private String fromUsername;
    private String fromUserAvatar;
}
