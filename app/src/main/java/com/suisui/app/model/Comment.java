package com.suisui.app.model;

/**
 * 评论
 */
public class Comment {
    private long id;
    private long momentId;
    private long userId;
    private String username;
    private String userAvatar;
    private String content;
    private long createdAt;

    public Comment() {}

    public Comment(long id, long momentId, long userId, String username, String userAvatar,
                   String content, long createdAt) {
        this.id = id;
        this.momentId = momentId;
        this.userId = userId;
        this.username = username;
        this.userAvatar = userAvatar;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMomentId() { return momentId; }
    public void setMomentId(long momentId) { this.momentId = momentId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
