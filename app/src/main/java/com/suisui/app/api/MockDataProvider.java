package com.suisui.app.api;

import com.suisui.app.model.Comment;
import com.suisui.app.model.Moment;
import com.suisui.app.model.Notification;
import com.suisui.app.model.User;
import com.suisui.app.util.DataGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 本地模拟数据提供者 - 替代后端 API，用于演示
 * 所有数据存储在内存中，重启应用后重置
 */
public class MockDataProvider {

    private static MockDataProvider instance;

    // 当前用户
    private User currentUser;
    private String currentToken = "mock-jwt-token-demo";

    // 所有用户（包括当前用户和发现用户）
    private final List<User> allUsers = new CopyOnWriteArrayList<>();
    // 订阅关系: userId -> 订阅了哪些 userId
    private final Map<Long, List<Long>> subscriptions = new HashMap<>();
    // 黑名单关系: userId -> 拉黑了哪些 userId
    private final Map<Long, List<Long>> blocklists = new HashMap<>();

    // 所有碎碎念: userId -> moments
    private final Map<Long, List<Moment>> userMoments = new HashMap<>();
    // 所有评论: momentId -> comments
    private final Map<Long, List<Comment>> momentComments = new HashMap<>();
    // 点赞关系: momentId -> 点赞的 userId 列表
    private final Map<Long, List<Long>> momentLikes = new HashMap<>();
    // 通知列表
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    private long nextMomentId = 10000;
    private long nextCommentId = 30000;
    private long nextNotificationId = 40000;
    private long nextUserId = 50000;

    private MockDataProvider() {
        initDemoData();
    }

    public static synchronized MockDataProvider getInstance() {
        if (instance == null) {
            instance = new MockDataProvider();
        }
        return instance;
    }

    /**
     * 初始化演示数据
     */
    private void initDemoData() {
        // 创建当前用户
        currentUser = DataGenerator.generateCurrentUser();
        allUsers.add(currentUser);

        // 创建发现用户
        List<User> discoverUsers = DataGenerator.generateDiscoverUsers();
        allUsers.addAll(discoverUsers);

        // 为每个用户生成碎碎念
        for (User user : allUsers) {
            List<Moment> moments = DataGenerator.generateMoments(
                    user.getId(), user.getNickname(), user.getAvatar(),
                    5 + (int) (Math.random() * 8));
            userMoments.put(user.getId(), new ArrayList<>(moments));

            // 为每个碎碎念生成评论
            for (Moment m : moments) {
                int commentCount = (int) (Math.random() * 4);
                List<Comment> comments = DataGenerator.generateComments(m.getId(), commentCount);
                momentComments.put(m.getId(), new ArrayList<>(comments));
                m.setCommentCount(comments.size());

                // 随机点赞
                List<Long> likers = new ArrayList<>();
                if (Math.random() > 0.3) {
                    likers.add(currentUser.getId());
                    m.setLiked(true);
                }
                for (int i = 0; i < m.getLikeCount(); i++) {
                    if (i < discoverUsers.size()) {
                        likers.add(discoverUsers.get(i).getId());
                    }
                }
                momentLikes.put(m.getId(), likers);
            }
        }

        // 初始化订阅: 当前用户订阅前5个发现用户
        List<Long> mySubs = new ArrayList<>();
        for (int i = 0; i < 5 && i < discoverUsers.size(); i++) {
            mySubs.add(discoverUsers.get(i).getId());
            discoverUsers.get(i).setSubscribed(true);
        }
        subscriptions.put(currentUser.getId(), mySubs);

        // 初始化黑名单
        blocklists.put(currentUser.getId(), new ArrayList<>());

        // 生成通知
        List<Notification> notifs = DataGenerator.generateNotifications(8);
        notifications.addAll(notifs);

        // 更新用户统计
        for (User user : allUsers) {
            List<Moment> ms = userMoments.get(user.getId());
            if (ms != null) {
                user.setMomentCount(ms.size());
            }
        }
    }

    // ======================== 认证 ========================

    public boolean login(String username, String password) {
        // 演示模式：接受任何用户名密码
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            currentUser.setUsername(username);
            currentUser.setNickname(username);
            currentToken = "mock-jwt-" + username;
            return true;
        }
        return false;
    }

    public boolean register(String username, String password) {
        if (username != null && username.length() >= 3 && password != null && password.length() >= 6) {
            currentUser.setUsername(username);
            currentUser.setNickname(username);
            currentToken = "mock-jwt-" + username;
            return true;
        }
        return false;
    }

    public String getToken() {
        return currentToken;
    }

    public long getCurrentUserId() {
        return currentUser.getId();
    }

    // ======================== 用户 / 世界 ========================

    public User getCurrentUser() {
        return currentUser;
    }

    public User getUser(long userId) {
        if (userId == currentUser.getId()) {
            return currentUser;
        }
        for (User u : allUsers) {
            if (u.getId() == userId) return u;
        }
        return null;
    }

    public void updateProfile(String nickname, String bio, String avatarUrl, String bgUrl) {
        if (nickname != null) currentUser.setNickname(nickname);
        if (bio != null) currentUser.setBio(bio);
        if (avatarUrl != null) currentUser.setAvatar(avatarUrl);
        if (bgUrl != null) currentUser.setBackgroundImage(bgUrl);
    }

    public void toggleWorldPublic() {
        currentUser.setWorldPublic(!currentUser.isWorldPublic());
    }

    public void changePassword(String oldPwd, String newPwd) {
        // 演示模式：总是成功
    }

    // ======================== 发现 ========================

    public List<User> getDiscoverUsers() {
        // 返回公开且未被当前用户拉黑的用户
        List<Long> blocked = blocklists.getOrDefault(currentUser.getId(), new ArrayList<>());
        return allUsers.stream()
                .filter(u -> u.getId() != currentUser.getId())
                .filter(User::isWorldPublic)
                .filter(u -> !blocked.contains(u.getId()))
                .peek(u -> {
                    List<Long> subs = subscriptions.getOrDefault(currentUser.getId(), new ArrayList<>());
                    u.setSubscribed(subs.contains(u.getId()));
                })
                .collect(Collectors.toList());
    }

    // ======================== 订阅 ========================

    public List<User> getSubscribedUsers() {
        List<Long> subIds = subscriptions.getOrDefault(currentUser.getId(), new ArrayList<>());
        return allUsers.stream()
                .filter(u -> subIds.contains(u.getId()))
                .peek(u -> u.setSubscribed(true))
                .collect(Collectors.toList());
    }

    public boolean toggleSubscription(long targetUserId) {
        List<Long> subs = subscriptions.computeIfAbsent(currentUser.getId(), k -> new ArrayList<>());
        boolean nowSubscribed;
        if (subs.contains(targetUserId)) {
            subs.remove(targetUserId);
            nowSubscribed = false;
        } else {
            subs.add(targetUserId);
            nowSubscribed = true;
        }
        // 更新被订阅用户的计数
        User target = getUser(targetUserId);
        if (target != null) {
            target.setSubscribed(nowSubscribed);
            target.setSubscriberCount(target.getSubscriberCount() + (nowSubscribed ? 1 : -1));
        }
        return nowSubscribed;
    }

    // ======================== 黑名单 ========================

    public List<User> getBlockedUsers() {
        List<Long> blockedIds = blocklists.getOrDefault(currentUser.getId(), new ArrayList<>());
        return allUsers.stream()
                .filter(u -> blockedIds.contains(u.getId()))
                .collect(Collectors.toList());
    }

    public boolean toggleBlock(long targetUserId) {
        List<Long> blocks = blocklists.computeIfAbsent(currentUser.getId(), k -> new ArrayList<>());
        if (blocks.contains(targetUserId)) {
            blocks.remove(targetUserId);
            return false;
        } else {
            blocks.add(targetUserId);
            // 同时取消订阅
            List<Long> subs = subscriptions.getOrDefault(currentUser.getId(), new ArrayList<>());
            subs.remove(targetUserId);
            return true;
        }
    }

    // ======================== 碎碎念 ========================

    public List<Moment> getUserMoments(long userId) {
        if (userId == currentUser.getId()) {
            List<Moment> moments = userMoments.getOrDefault(userId, new ArrayList<>());
            // 过滤已删除，设置当前用户信息
            return moments.stream()
                    .filter(m -> !m.isDeleted())
                    .peek(m -> {
                        m.setUserNickname(currentUser.getNickname());
                        m.setUserAvatar(currentUser.getAvatar());
                    })
                    .collect(Collectors.toList());
        }
        User u = getUser(userId);
        List<Moment> moments = userMoments.getOrDefault(userId, new ArrayList<>());
        return moments.stream()
                .filter(m -> !m.isDeleted())
                .peek(m -> {
                    if (u != null) {
                        m.setUserNickname(u.getNickname());
                        m.setUserAvatar(u.getAvatar());
                    }
                })
                .collect(Collectors.toList());
    }

    public Moment getMomentDetail(long momentId) {
        for (List<Moment> moments : userMoments.values()) {
            for (Moment m : moments) {
                if (m.getId() == momentId) {
                    // 设置用户信息
                    User u = getUser(m.getUserId());
                    if (u != null) {
                        m.setUserNickname(u.getNickname());
                        m.setUserAvatar(u.getAvatar());
                    }
                    return m;
                }
            }
        }
        return null;
    }

    public Moment createMoment(String content, List<String> images, List<String> tags) {
        Moment moment = new Moment();
        moment.setId(nextMomentId++);
        moment.setUserId(currentUser.getId());
        moment.setContent(content);
        moment.setImages(images != null ? images : new ArrayList<>());
        moment.setTags(tags != null ? tags : new ArrayList<>());
        moment.setLikeCount(0);
        moment.setCommentCount(0);
        moment.setLiked(false);
        moment.setCreatedAt(System.currentTimeMillis());
        moment.setUpdatedAt(System.currentTimeMillis());
        moment.setUserNickname(currentUser.getNickname());
        moment.setUserAvatar(currentUser.getAvatar());

        userMoments.computeIfAbsent(currentUser.getId(), k -> new ArrayList<>()).add(0, moment);
        momentComments.put(moment.getId(), new ArrayList<>());
        momentLikes.put(moment.getId(), new ArrayList<>());

        // 更新用户统计
        List<Moment> ms = userMoments.get(currentUser.getId());
        currentUser.setMomentCount(ms != null ? ms.size() : 0);

        return moment;
    }

    public boolean deleteMoment(long momentId) {
        List<Moment> moments = userMoments.get(currentUser.getId());
        if (moments != null) {
            for (Moment m : moments) {
                if (m.getId() == momentId) {
                    m.setDeleted(true);
                    currentUser.setMomentCount(currentUser.getMomentCount() - 1);
                    return true;
                }
            }
        }
        return false;
    }

    // ======================== 点赞 ========================

    public boolean toggleLike(long momentId) {
        List<Long> likers = momentLikes.computeIfAbsent(momentId, k -> new ArrayList<>());
        boolean nowLiked;
        if (likers.contains(currentUser.getId())) {
            likers.remove(currentUser.getId());
            nowLiked = false;
        } else {
            likers.add(currentUser.getId());
            nowLiked = true;
        }
        // 更新 moment 的计数
        Moment moment = getMomentDetail(momentId);
        if (moment != null) {
            moment.setLiked(nowLiked);
            moment.setLikeCount(likers.size());
        }
        return nowLiked;
    }

    public boolean isLiked(long momentId) {
        List<Long> likers = momentLikes.getOrDefault(momentId, new ArrayList<>());
        return likers.contains(currentUser.getId());
    }

    // ======================== 评论 ========================

    public List<Comment> getComments(long momentId) {
        return momentComments.getOrDefault(momentId, new ArrayList<>());
    }

    public Comment createComment(long momentId, String content) {
        Comment comment = new Comment();
        comment.setId(nextCommentId++);
        comment.setMomentId(momentId);
        comment.setUserId(currentUser.getId());
        comment.setUsername(currentUser.getNickname());
        comment.setContent(content);
        comment.setCreatedAt(System.currentTimeMillis());

        momentComments.computeIfAbsent(momentId, k -> new ArrayList<>()).add(comment);

        // 更新 moment 评论计数
        Moment moment = getMomentDetail(momentId);
        if (moment != null) {
            moment.setCommentCount(momentComments.get(momentId).size());
        }

        return comment;
    }

    // ======================== 通知 ========================

    public List<Notification> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void clearNotifications() {
        notifications.clear();
    }

    // ======================== 统计 ========================

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Moment> myMoments = userMoments.getOrDefault(currentUser.getId(), new ArrayList<>());
        List<Moment> activeMoments = myMoments.stream()
                .filter(m -> !m.isDeleted())
                .collect(Collectors.toList());

        stats.put("totalNotes", activeMoments.size());
        stats.put("totalDays", Math.max(1, activeMoments.size() / 2 + (int) (Math.random() * 5)));
        stats.put("consecutiveDays", Math.min(7, activeMoments.size()));

        // 标签排行
        Map<String, Integer> tagCount = new HashMap<>();
        for (Moment m : activeMoments) {
            if (m.getTags() != null) {
                for (String tag : m.getTags()) {
                    tagCount.merge(tag, 1, Integer::sum);
                }
            }
        }
        List<Map<String, Object>> tagRank = tagCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(8)
                .map(e -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("tag", e.getKey());
                    entry.put("count", e.getValue());
                    return entry;
                })
                .collect(Collectors.toList());
        stats.put("tagRank", tagRank);

        // 周趋势
        List<Map<String, Object>> weeklyTrend = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        long dayMs = 86400000L;
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", sdf.format(new java.util.Date(System.currentTimeMillis() - i * dayMs)));
            day.put("count", (int) (Math.random() * 5));
            weeklyTrend.add(day);
        }
        stats.put("weeklyTrend", weeklyTrend);

        return stats;
    }

    /**
     * 重置所有数据（用于退出登录）
     */
    public void reset() {
        allUsers.clear();
        subscriptions.clear();
        blocklists.clear();
        userMoments.clear();
        momentComments.clear();
        momentLikes.clear();
        notifications.clear();
        nextMomentId = 10000;
        nextCommentId = 30000;
        nextNotificationId = 40000;
        initDemoData();
    }
}
