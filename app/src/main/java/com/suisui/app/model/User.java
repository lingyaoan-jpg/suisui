package com.suisui.app.model;

/**
 * 用户信息
 */
public class User {
    private long id;
    private String username;
    private String password;          // 仅本地使用
    private String nickname;
    private String avatar;            // 头像URL
    private String backgroundImage;   // 顶部背景图URL
    private String bio;               // 个人介绍
    private boolean isWorldPublic;    // 世界是否公开
    private long lastActiveTime;      // 最后活跃时间（发布碎碎念）
    private int subscriberCount;      // 订阅者数量
    private int momentCount;          // 碎碎念总数
    private boolean isSubscribed;     // 当前用户是否已订阅

    public User() {}

    public User(long id, String username, String nickname) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.isWorldPublic = true;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isWorldPublic() { return isWorldPublic; }
    public void setWorldPublic(boolean worldPublic) { isWorldPublic = worldPublic; }

    public long getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(long lastActiveTime) { this.lastActiveTime = lastActiveTime; }

    public int getSubscriberCount() { return subscriberCount; }
    public void setSubscriberCount(int subscriberCount) { this.subscriberCount = subscriberCount; }

    public int getMomentCount() { return momentCount; }
    public void setMomentCount(int momentCount) { this.momentCount = momentCount; }

    public boolean isSubscribed() { return isSubscribed; }
    public void setSubscribed(boolean subscribed) { isSubscribed = subscribed; }
}
