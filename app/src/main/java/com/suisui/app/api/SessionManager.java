package com.suisui.app.api;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * JWT Token 和用户信息本地存储
 */
public class SessionManager {

    private static final String PREF_NAME = "suisui_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_AVATAR = "avatar_url";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // --- Token ---

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    // --- User Info ---

    public void saveUserInfo(long userId, String username, String nickname, String avatarUrl) {
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_NICKNAME, nickname)
                .putString(KEY_AVATAR, avatarUrl)
                .apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getNickname() {
        return prefs.getString(KEY_NICKNAME, "");
    }

    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR, "");
    }

    public void saveAvatarUrl(String avatarUrl) {
        prefs.edit().putString(KEY_AVATAR, avatarUrl).apply();
    }

    public void saveNickname(String nickname) {
        prefs.edit().putString(KEY_NICKNAME, nickname).apply();
    }

    // --- Logout ---

    public void clear() {
        prefs.edit().clear().apply();
    }
}
