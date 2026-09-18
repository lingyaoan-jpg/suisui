package com.suisui.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.suisui.app.MainActivity;
import com.suisui.app.R;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.api.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = SessionManager.getInstance(this);
        apiService = ApiClient.getInstance(this).getService();

        // 已经登录则直接跳转
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> register());
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<ApiService.ApiResponse<ApiService.AuthData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.AuthData>> call,
                                   Response<ApiService.ApiResponse<ApiService.AuthData>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse<ApiService.AuthData> result = response.body();
                    if (result.code == 200 && result.data != null) {
                        sessionManager.saveToken(result.data.token);
                        sessionManager.saveUserInfo(result.data.userId,
                                result.data.username,
                                result.data.nickname != null ? result.data.nickname : result.data.username,
                                result.data.avatarUrl != null ? result.data.avatarUrl : "");
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, result.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 尝试从错误响应中读取服务器返回的错误消息
                    String errorMsg = "登录失败，请检查网络连接";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            ApiService.ApiResponse<?> errorResp = gson.fromJson(errorJson, ApiService.ApiResponse.class);
                            if (errorResp != null && errorResp.message != null) {
                                errorMsg = errorResp.message;
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.AuthData>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 3) {
            Toast.makeText(this, "用户名至少3位", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        apiService.register(body).enqueue(new Callback<ApiService.ApiResponse<ApiService.AuthData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.AuthData>> call,
                                   Response<ApiService.ApiResponse<ApiService.AuthData>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse<ApiService.AuthData> result = response.body();
                    if (result.code == 200 && result.data != null) {
                        sessionManager.saveToken(result.data.token);
                        sessionManager.saveUserInfo(result.data.userId,
                                result.data.username,
                                result.data.nickname != null ? result.data.nickname : result.data.username,
                                result.data.avatarUrl != null ? result.data.avatarUrl : "");
                        Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, result.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 尝试从错误响应中读取服务器返回的错误消息
                    String errorMsg = "注册失败，请检查网络连接";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            ApiService.ApiResponse<?> errorResp = gson.fromJson(errorJson, ApiService.ApiResponse.class);
                            if (errorResp != null && errorResp.message != null) {
                                errorMsg = errorResp.message;
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.AuthData>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        btnLogin.setEnabled(!loading);
        btnRegister.setEnabled(!loading);
    }
}
