package com.suisui.app.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.suisui.app.R;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.api.SessionManager;
import com.suisui.app.databinding.FragmentMeBinding;
import com.suisui.app.util.ImageLoader;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeFragment extends Fragment {

    private FragmentMeBinding binding;
    private ApiService apiService;
    private SessionManager sessionManager;
    private boolean worldPublic = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getInstance(requireContext()).getService();
        sessionManager = SessionManager.getInstance(requireContext());

        initViews();
        loadWorldStatus();
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 从编辑资料页返回后刷新头像和昵称
        binding.tvNickname.setText(sessionManager.getNickname());
        String avatar = sessionManager.getAvatarUrl();
        if (avatar != null && !avatar.isEmpty()) {
            ImageLoader.loadAvatar(requireContext(), avatar, binding.ivAvatar);
        }
        loadWorldStatus();
    }

    private void initViews() {
        binding.tvNickname.setText(sessionManager.getNickname());
        binding.tvUsername.setText("@" + sessionManager.getUsername());
        ImageLoader.loadAvatar(requireContext(), sessionManager.getAvatarUrl(), binding.ivAvatar);
    }

    private void loadWorldStatus() {
        long userId = sessionManager.getUserId();
        apiService.getWorld(userId).enqueue(new Callback<ApiService.ApiResponse<ApiService.WorldData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.WorldData>> call,
                                   Response<ApiService.ApiResponse<ApiService.WorldData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiService.WorldData world = response.body().data;
                    worldPublic = world.worldPublic;
                    binding.switchPublic.setChecked(worldPublic);
                    if (world.avatarUrl != null && !world.avatarUrl.isEmpty()) {
                        sessionManager.saveAvatarUrl(world.avatarUrl);
                        ImageLoader.loadAvatar(requireContext(), world.avatarUrl, binding.ivAvatar);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.WorldData>> call, Throwable t) {
                // 静默失败
            }
        });
    }

    private void setupClickListeners() {
        binding.layoutUserHeader.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), EditProfileActivity.class));
        });

        binding.switchPublic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            apiService.toggleWorldPublic().enqueue(new Callback<ApiService.ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiService.ApiResponse<Void>> call,
                                       Response<ApiService.ApiResponse<Void>> response) {
                    worldPublic = isChecked;
                    String msg = isChecked ? "世界已公开" : "世界已关闭，仅自己可见";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<ApiService.ApiResponse<Void>> call, Throwable t) {
                    binding.switchPublic.setChecked(!isChecked);
                    Toast.makeText(requireContext(), "切换失败", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationActivity.class)));

        binding.btnStatistics.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), StatisticsActivity.class)));

        binding.btnSubscriptionManage.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SubscriptionManageActivity.class)));

        binding.btnBlacklist.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), BlacklistManageActivity.class)));

        binding.btnExport.setOnClickListener(v ->
                Toast.makeText(requireContext(), "正在打包数据...", Toast.LENGTH_SHORT).show());

        binding.btnAccountSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AccountSettingsActivity.class)));

        binding.btnAbout.setOnClickListener(v ->
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("关于碎碎念")
                        .setMessage("碎碎念 v1.0\n\n一个极简的个人碎碎念记录工具。\n记录生活的小确幸 ✨")
                        .setPositiveButton("确定", null)
                        .show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
