package com.suisui.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 碎碎念 - 一条记录
 */
public class Moment {
    private long id;
    private long userId;
    private String content;
    private List<String> images;      // 图片URL列表，最多3张
    private List<String> tags;        // 标签列表
    private int likeCount;
    private int commentCount;
    private boolean isLiked;          // 当前用户是否已点赞
    private long createdAt;
    private long updatedAt;
    private boolean isDeleted;

    // 关联用户信息（展示用）
    private String username;
    private String userNickname;
    private String userAvatar;

    public Moment() {
        this.images = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Moment(long id, long userId, String content, List<String> images, List<String> tags,
                  int likeCount, int commentCount, long createdAt, long updatedAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.images = images != null ? images : new ArrayList<>();
        this.tags = tags != null ? tags : new ArrayList<>();
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images != null ? images : new ArrayList<>(); }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public boolean hasImages() { return images != null && !images.isEmpty(); }

    /**
     * 获取需要显示的文本（超过5行折叠）
     */
    public boolean isTextOverflow() {
        return content != null && content.length() > 140; // 约5行的字符数
    }
}
