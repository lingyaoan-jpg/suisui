package com.suisui.app.api;

import android.content.Context;

import com.suisui.app.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 客户端单例
 * 服务端地址由 BuildConfig.BASE_URL 提供，来源见 local.properties 的 suisui.serverUrl
 */
public class ApiClient {

    // 服务端地址在构建时注入，见 local.properties 的 suisui.serverUrl
    // 未配置时默认 http://10.0.2.2:8080/（Android 模拟器访问宿主机的固定地址）
    // 真机调试请在 local.properties 里填写电脑的局域网 IP
    public static final String BASE_URL = BuildConfig.BASE_URL;

    private static ApiClient instance;
    private final Retrofit retrofit;
    private final ApiService apiService;
    private SessionManager sessionManager;

    private ApiClient(Context context) {
        sessionManager = SessionManager.getInstance(context);

        // 日志拦截器（Debug 模式）
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // JWT 认证拦截器
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            String token = sessionManager.getToken();
            if (token != null && !token.isEmpty()) {
                Request request = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(request);
            }
            return chain.proceed(original);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public ApiService getService() {
        return apiService;
    }

    public SessionManager getSession() {
        return sessionManager;
    }
}
