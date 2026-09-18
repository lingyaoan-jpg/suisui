package com.suisui.app.api;

import com.suisui.app.model.*;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * 碎碎念后端 REST API 接口定义
 */
public interface ApiService {

    // ========== 认证 ==========

    @POST("api/auth/register")
    Call<ApiResponse<AuthData>> register(@Body Map<String, String> body);

    @POST("api/auth/login")
    Call<ApiResponse<AuthData>> login(@Body Map<String, String> body);

    @PUT("api/auth/password")
    Call<ApiResponse<Void>> changePassword(@Body Map<String, String> body);

    @PUT("api/user/profile")
    Call<ApiResponse<Void>> updateProfile(@Body Map<String, String> body);

    // ========== 用户世界 ==========

    @GET("api/world/{userId}")
    Call<ApiResponse<WorldData>> getWorld(@Path("userId") long userId);

    @PUT("api/world/public")
    Call<ApiResponse<Void>> toggleWorldPublic();

    @GET("api/world/discover")
    Call<ApiResponse<List<WorldData>>> getDiscover(@Query("page") int page, @Query("size") int size);

    // ========== 碎碎念 ==========

    @GET("api/notes/{userId}")
    Call<ApiResponse<PageData<NoteData>>> getUserNotes(
            @Path("userId") long userId,
            @Query("page") int page,
            @Query("size") int size,
            @Query("tag") String tag);

    @GET("api/notes/detail/{noteId}")
    Call<ApiResponse<NoteData>> getNoteDetail(@Path("noteId") long noteId);

    @POST("api/notes")
    Call<ApiResponse<NoteData>> createNote(@Body Map<String, Object> body);

    @DELETE("api/notes/{noteId}")
    Call<ApiResponse<Void>> deleteNote(@Path("noteId") long noteId);

    // ========== 点赞 ==========

    @POST("api/likes")
    Call<ApiResponse<Map<String, Object>>> toggleLike(@Body Map<String, Long> body);

    // ========== 评论 ==========

    @GET("api/comments/{noteId}")
    Call<ApiResponse<PageData<CommentData>>> getComments(
            @Path("noteId") long noteId,
            @Query("page") int page,
            @Query("size") int size);

    @POST("api/comments")
    Call<ApiResponse<CommentData>> createComment(@Body Map<String, Object> body);

    // ========== 订阅 ==========

    @GET("api/subscriptions")
    Call<ApiResponse<List<WorldData>>> getSubscriptions();

    @PUT("api/subscriptions/{targetUserId}")
    Call<ApiResponse<Map<String, Object>>> toggleSubscription(@Path("targetUserId") long targetUserId);

    // ========== 黑名单 ==========

    @GET("api/blocklist")
    Call<ApiResponse<List<WorldData>>> getBlocklist();

    @PUT("api/blocklist/{targetUserId}")
    Call<ApiResponse<Map<String, Object>>> toggleBlock(@Path("targetUserId") long targetUserId);

    // ========== 通知 ==========

    @GET("api/notifications")
    Call<ApiResponse<PageData<NotificationData>>> getNotifications(
            @Query("page") int page,
            @Query("size") int size);

    @DELETE("api/notifications")
    Call<ApiResponse<Void>> clearNotifications();

    // ========== 统计 ==========

    @GET("api/stats")
    Call<ApiResponse<StatsData>> getStats();

    // ========== 图片 ==========

    @Multipart
    @POST("api/images/upload")
    Call<Map<String, Object>> uploadImage(@Part MultipartBody.Part file);

    // ========== 数据模型 ==========

    class ApiResponse<T> {
        public int code;
        public String message;
        public T data;
    }

    class AuthData {
        public long userId;
        public String token;
        public String username;
        public String nickname;
        public String avatarUrl;
    }

    class WorldData {
        public long userId;
        public String nickname;
        public String username;
        public String avatarUrl;
        public String bgUrl;
        public String bio;
        public boolean worldPublic;
        public String lastActiveTime;
        public int subscriberCount;
        public int momentCount;
        public boolean subscribed;
        public String latestMomentPreview;
    }

    class NoteData {
        public long id;
        public long userId;
        public String content;
        public List<String> images;
        public List<String> tags;
        public int likeCount;
        public int commentCount;
        public boolean isLiked;
        public String createdAt;
        public String updatedAt;
        public String username;
        public String userNickname;
        public String userAvatar;
    }

    class CommentData {
        public long id;
        public long noteId;
        public long userId;
        public String username;
        public String userAvatar;
        public String content;
        public String createdAt;
    }

    class NotificationData {
        public long id;
        public long userId;
        public long fromUserId;
        public String type;
        public long noteId;
        public String contentPreview;
        public boolean isRead;
        public String createdAt;

        public String fromUsername;
        public String fromUserAvatar;
    }

    class StatsData {
        public long totalNotes;
        public long totalDays;
        public int consecutiveDays;
        public List<TagRankData> tagRank;
        public List<DailyCountData> weeklyTrend;
    }

    class TagRankData {
        public String tag;
        public int count;
    }

    class DailyCountData {
        public String date;
        public int count;
    }

    class PageData<T> {
        public List<T> content;
        public int totalPages;
        public long totalElements;
        public int number;
        public int size;
    }
}
