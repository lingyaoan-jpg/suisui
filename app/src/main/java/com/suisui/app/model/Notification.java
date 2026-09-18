package com.suisui.app.model;

/**
 * 通知 - 点赞或评论通知
 */
public class Notification {
    public static final int TYPE_LIKE = 1;
    public static final int TYPE_COMMENT = 2;

    private long id;
    private int type;               // 1=点赞, 2=评论
    private long fromUserId;
    private String fromUsername;
    private String fromUserAvatar;
    private long momentId;
    private String commentContent;  // 评论内容（仅TYPE_COMMENT时有值）
    private long createdAt;

    public Notification() {}

    public Notification(long id, int type, long fromUserId, String fromUsername,
                        String fromUserAvatar, long momentId, String commentContent, long createdAt) {
        this.id = id;
        this.type = type;
        this.fromUserId = fromUserId;
        this.fromUsername = fromUsername;
        this.fromUserAvatar = fromUserAvatar;
        this.momentId = momentId;
        this.commentContent = commentContent;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public long getFromUserId() { return fromUserId; }
    public void setFromUserId(long fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }

    public String getFromUserAvatar() { return fromUserAvatar; }
    public void setFromUserAvatar(String fromUserAvatar) { this.fromUserAvatar = fromUserAvatar; }

    public long getMomentId() { return momentId; }
    public void setMomentId(long momentId) { this.momentId = momentId; }

    public String getCommentContent() { return commentContent; }
    public void setCommentContent(String commentContent) { this.commentContent = commentContent; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getActionDescription() {
        if (type == TYPE_LIKE) {
            return "赞了你的碎碎念";
        } else if (type == TYPE_COMMENT) {
            String preview = commentContent != null && commentContent.length() > 20
                    ? commentContent.substring(0, 20) + "..."
                    : commentContent;
            return "评论了你的碎碎念：" + preview;
        }
        return "";
    }
}
