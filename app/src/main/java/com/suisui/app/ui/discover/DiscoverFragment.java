package com.suisui.app.ui.discover;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.suisui.app.R;
import com.suisui.app.adapter.WorldCardAdapter;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.databinding.FragmentDiscoverBinding;
import com.suisui.app.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoverFragment extends Fragment {

    private FragmentDiscoverBinding binding;
    private WorldCardAdapter worldCardAdapter;
    private ApiService apiService;

    private List<User> discoverUsers = new ArrayList<>();
    private List<User> subscribedUsers = new ArrayList<>();
    private int currentTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getInstance(requireContext()).getService();

        initAdapters();
        loadData();
        setupTabListener();
    }

    private void initAdapters() {
        worldCardAdapter = new WorldCardAdapter((user, position) -> {
            Intent intent = new Intent(requireContext(), OtherWorldActivity.class);
            intent.putExtra("user_id", user.getId());
            intent.putExtra("nickname", user.getNickname());
            startActivity(intent);
        });
        binding.rvWorlds.setAdapter(worldCardAdapter);
    }

    private void loadData() {
        loadDiscover();
        loadSubscriptions();
    }

    private void loadDiscover() {
        apiService.getDiscover(1, 50).enqueue(new Callback<ApiService.ApiResponse<List<ApiService.WorldData>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<ApiService.WorldData>>> call,
                                   Response<ApiService.ApiResponse<List<ApiService.WorldData>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    discoverUsers.clear();
                    for (ApiService.WorldData wd : response.body().data) {
                        User u = new User();
                        u.setId(wd.userId);
                        u.setNickname(wd.nickname);
                        u.setUsername(wd.username);
                        u.setAvatar(wd.avatarUrl);
                        u.setBackgroundImage(wd.bgUrl);
                        u.setBio(wd.bio);
                        u.setWorldPublic(wd.worldPublic);
                        u.setSubscriberCount(wd.subscriberCount);
                        u.setMomentCount(wd.momentCount);
                        u.setSubscribed(wd.subscribed);
                        discoverUsers.add(u);
                    }
                    updateWorldList();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<ApiService.WorldData>>> call, Throwable t) {
                Toast.makeText(requireContext(), "加载失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSubscriptions() {
        apiService.getSubscriptions().enqueue(new Callback<ApiService.ApiResponse<List<ApiService.WorldData>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<ApiService.WorldData>>> call,
                                   Response<ApiService.ApiResponse<List<ApiService.WorldData>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    subscribedUsers.clear();
                    for (ApiService.WorldData wd : response.body().data) {
                        User u = new User();
                        u.setId(wd.userId);
                        u.setNickname(wd.nickname);
                        u.setUsername(wd.username);
                        u.setAvatar(wd.avatarUrl);
                        u.setBackgroundImage(wd.bgUrl);
                        u.setBio(wd.bio);
                        u.setSubscribed(true);
                        subscribedUsers.add(u);
                    }
                    updateWorldList();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<ApiService.WorldData>>> call, Throwable t) {
                Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTabListener() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                updateWorldList();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateWorldList() {
        List<User> list = currentTab == 0 ? discoverUsers : subscribedUsers;
        worldCardAdapter.setUsers(list);
        binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvWorlds.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        if (list.isEmpty()) {
            binding.tvEmpty.setText(currentTab == 0 ? R.string.no_public_worlds : R.string.no_subscriptions);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
