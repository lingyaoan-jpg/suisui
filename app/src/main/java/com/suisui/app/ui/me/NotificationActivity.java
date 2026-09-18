package com.suisui.app.ui.me;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.suisui.app.adapter.NotificationAdapter;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.databinding.ActivityNotificationBinding;
import com.suisui.app.model.Notification;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private NotificationAdapter adapter;
    private ApiService apiService;
    private List<Notification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance(this).getService();
        initViews();
        loadNotifications();
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new NotificationAdapter((notification, position) -> {
            Toast.makeText(this, "跳转到对应碎碎念...", Toast.LENGTH_SHORT).show();
            finish();
        });
        binding.rvNotifications.setAdapter(adapter);

        binding.btnClearAll.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("清空通知")
                    .setMessage("确定要清空所有通知吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        apiService.clearNotifications().enqueue(new Callback<ApiService.ApiResponse<Void>>() {
                            @Override
                            public void onResponse(Call<ApiService.ApiResponse<Void>> call,
                                                   Response<ApiService.ApiResponse<Void>> response) {
                                notifications.clear();
                                adapter.setNotifications(notifications);
                                updateEmptyState();
                                Toast.makeText(NotificationActivity.this, "已清空", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<ApiService.ApiResponse<Void>> call, Throwable t) {
                                Toast.makeText(NotificationActivity.this, "清空失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void loadNotifications() {
        apiService.getNotifications(1, 100).enqueue(
                new Callback<ApiService.ApiResponse<ApiService.PageData<ApiService.NotificationData>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.PageData<ApiService.NotificationData>>> call,
                                   Response<ApiService.ApiResponse<ApiService.PageData<ApiService.NotificationData>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    notifications.clear();
                    for (ApiService.NotificationData nd : response.body().data.content) {
                        Notification n = new Notification();
                        n.setId(nd.id);
                        n.setType(nd.type.equals("LIKE") ? Notification.TYPE_LIKE : Notification.TYPE_COMMENT);
                        n.setFromUserId(nd.fromUserId);
                        n.setFromUsername(nd.fromUsername);
                        n.setFromUserAvatar(nd.fromUserAvatar);
                        n.setMomentId(nd.noteId);
                        n.setCommentContent(nd.contentPreview);
                        n.setCreatedAt(com.suisui.app.util.TimeUtils.parseServerDate(nd.createdAt));
                        notifications.add(n);
                    }
                    adapter.setNotifications(notifications);
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.PageData<ApiService.NotificationData>>> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (notifications.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvNotifications.setVisibility(View.GONE);
            binding.btnClearAll.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvNotifications.setVisibility(View.VISIBLE);
            binding.btnClearAll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
