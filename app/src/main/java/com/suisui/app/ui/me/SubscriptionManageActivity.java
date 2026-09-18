package com.suisui.app.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.suisui.app.adapter.WorldCardAdapter;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.databinding.ActivitySubscriptionManageBinding;
import com.suisui.app.model.User;
import com.suisui.app.ui.discover.OtherWorldActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionManageActivity extends AppCompatActivity {

    private ActivitySubscriptionManageBinding binding;
    private WorldCardAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubscriptionManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance(this).getService();
        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new WorldCardAdapter((user, position) -> {
            Intent intent = new Intent(this, OtherWorldActivity.class);
            intent.putExtra("user_id", user.getId());
            intent.putExtra("nickname", user.getNickname());
            startActivity(intent);
        });
        binding.rvSubscriptions.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        apiService.getSubscriptions().enqueue(new Callback<ApiService.ApiResponse<List<ApiService.WorldData>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<ApiService.WorldData>>> call,
                                   Response<ApiService.ApiResponse<List<ApiService.WorldData>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<User> users = new ArrayList<>();
                    for (ApiService.WorldData wd : response.body().data) {
                        User u = new User();
                        u.setId(wd.userId);
                        u.setNickname(wd.nickname);
                        u.setUsername(wd.username);
                        u.setAvatar(wd.avatarUrl);
                        u.setBackgroundImage(wd.bgUrl);
                        u.setBio(wd.bio);
                        u.setSubscribed(true);
                        users.add(u);
                    }
                    adapter.setUsers(users);
                    if (users.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        binding.rvSubscriptions.setVisibility(View.GONE);
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                        binding.rvSubscriptions.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<ApiService.WorldData>>> call, Throwable t) {
                Toast.makeText(SubscriptionManageActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
