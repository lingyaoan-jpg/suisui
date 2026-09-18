package com.suisui.app.util;

import com.suisui.app.model.Comment;
import com.suisui.app.model.Moment;
import com.suisui.app.model.Notification;
import com.suisui.app.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 生成模拟数据，用于前端演示
 */
public class DataGenerator {

    private static final String[] SAMPLE_TEXTS = {
            "今天天气真好，出门散步晒太阳☀️ 感觉整个人都充满能量了！",
            "读到一句很棒的话：「生活不是等待暴风雨过去，而是学会在雨中跳舞。」\n\n共勉。",
            "新买的咖啡豆香气太迷人了，周末的早晨就应该被咖啡香唤醒 ☕",
            "完成了今天的TODO List！\n✅ 晨跑5公里\n✅ 读完第三章\n✅ 整理书桌\n✅ 做一顿健康晚餐\n\n虽然是小事，但完成的感觉真好～",
            "最近在学做手冲咖啡，第一杯虽然不太成功，但过程很有趣。有时候慢下来做一件事，本身就是享受。",
            "深夜突然想起小时候的夏天，外婆的蒲扇，井水冰过的西瓜，还有听不完的故事。\n\n那些回不去的时光啊。",
            "今天工作特别顺利，解决了一个困扰很久的bug，开心！果然有时候放一放再回头，思路会清晰很多。",
            "推荐一部最近看的好电影《完美的日子》，关于一个东京厕所清洁工的日常。平淡却深刻，看完后对生活有了新的理解。",
            "整理手机相册，发现了好多旅行时拍的照片。好想再去一次海边啊 🌊",
            "新入手了一盆多肉，希望能养活它 🙏 植物杀手最后一次尝试",
            "今天做的红烧肉意外好吃！秘诀是加了冰糖和一点点五香粉，入口即化！",
            "晚上路过一家书店，被橱窗里的灯光吸引走进去，不知不觉待了一个小时。实体书店的魅力永远无法被替代 📚"
    };

    private static final String[] SAMPLE_TAGS = {"日常", "感悟", "美食", "旅行", "学习", "工作", "读书", "摄影", "音乐", "运动", "碎碎念", "好物分享", "成长", "治愈", "心情"};

    private static final String[] SAMPLE_NICKNAMES = {"小明", "小红", "大白", "阿花", "老王", "小雨", "星辰", "海风", "山月", "云朵"};
    private static final String[] SAMPLE_COMMENTS = {
            "加油！", "太棒了 👏", "同感！", "哈哈哈哈哈", "好美好啊", "学到了！",
            "这个真的很不错", "羡慕！", "喜欢这种生活方式", "我也是这么想的",
            "可以分享一下吗？", "好好看！", "温暖～", "马克一下", "说得真好"
    };

    /**
     * 生成模拟的当前用户
     */
    public static User generateCurrentUser() {
        User user = new User(1, "suisui_user", "碎碎念小主");
        user.setAvatar("");
        user.setBackgroundImage("");
        user.setBio("记录生活的小确幸 ✨\n热爱美食、旅行和一切美好的事物。");
        user.setWorldPublic(true);
        user.setLastActiveTime(System.currentTimeMillis());
        user.setSubscriberCount(128);
        user.setMomentCount(42);
        return user;
    }

    /**
     * 生成模拟的碎碎念列表
     */
    public static List<Moment> generateMoments(long userId, String nickname, String avatar, int count) {
        List<Moment> moments = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            Moment moment = new Moment();
            moment.setId(1000 + i);
            moment.setUserId(userId);
            moment.setContent(SAMPLE_TEXTS[i % SAMPLE_TEXTS.length]);
            moment.setTags(Arrays.asList(SAMPLE_TAGS[i % SAMPLE_TAGS.length], SAMPLE_TAGS[(i + 3) % SAMPLE_TAGS.length]));
            moment.setLikeCount((int) (Math.random() * 50));
            moment.setCommentCount((int) (Math.random() * 10));
            moment.setLiked(Math.random() > 0.5);
            moment.setCreatedAt(now - i * 3600000L * (1 + (long) (Math.random() * 24)));
            moment.setUpdatedAt(moment.getCreatedAt());
            moment.setDeleted(false);
            moment.setUserNickname(nickname);
            moment.setUserAvatar(avatar);
            moments.add(moment);
        }

        return moments;
    }

    /**
     * 生成模拟的发现用户列表
     */
    public static List<User> generateDiscoverUsers() {
        List<User> users = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (int i = 0; i < 15; i++) {
            User user = new User(2000 + i, "user_" + i, SAMPLE_NICKNAMES[i % SAMPLE_NICKNAMES.length]);
            user.setBio("这是我的小世界 🌍");
            user.setLastActiveTime(now - i * 3600000L * (1 + (long) (Math.random() * 10)));
            user.setSubscriberCount((int) (Math.random() * 200));
            user.setMomentCount((int) (Math.random() * 100));
            user.setWorldPublic(true);
            user.setSubscribed(i < 5); // 前5个已订阅
            users.add(user);
        }

        return users;
    }

    /**
     * 生成模拟的评论列表
     */
    public static List<Comment> generateComments(long momentId, int count) {
        List<Comment> comments = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            Comment comment = new Comment();
            comment.setId(3000 + i);
            comment.setMomentId(momentId);
            comment.setUserId(2000 + i);
            comment.setUsername(SAMPLE_NICKNAMES[i % SAMPLE_NICKNAMES.length]);
            comment.setContent(SAMPLE_COMMENTS[i % SAMPLE_COMMENTS.length]);
            comment.setCreatedAt(now - i * 600000L * (1 + (long) (Math.random() * 10)));
            comments.add(comment);
        }

        return comments;
    }

    /**
     * 生成模拟的通知列表
     */
    public static List<Notification> generateNotifications(int count) {
        List<Notification> notifications = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            Notification notification = new Notification();
            notification.setId(4000 + i);
            notification.setType(i % 3 == 0 ? Notification.TYPE_LIKE : Notification.TYPE_COMMENT);
            notification.setFromUserId(2000 + i);
            notification.setFromUsername(SAMPLE_NICKNAMES[i % SAMPLE_NICKNAMES.length]);
            notification.setMomentId(1000 + (i % 12));
            if (notification.getType() == Notification.TYPE_COMMENT) {
                notification.setCommentContent(SAMPLE_COMMENTS[i % SAMPLE_COMMENTS.length]);
            }
            notification.setCreatedAt(now - i * 1800000L * (1 + (long) (Math.random() * 5)));
            notifications.add(notification);
        }

        return notifications;
    }

    /**
     * 获取所有标签（从模拟数据）
     */
    public static List<String> getAllTags() {
        return Arrays.asList(SAMPLE_TAGS);
    }
}
