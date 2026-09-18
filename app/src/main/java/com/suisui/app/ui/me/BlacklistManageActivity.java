package com.suisui.app.ui.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.suisui.app.R;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.databinding.ActivityBlacklistManageBinding;
import com.suisui.app.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlacklistManageActivity extends AppCompatActivity {

    private ActivityBlacklistManageBinding binding;
    private BlacklistAdapter adapter;
    private ApiService apiService;
    private List<BlockEntry> blacklist = new ArrayList<>();

    private static class BlockEntry {
        User user;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlacklistManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance(this).getService();
        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new BlacklistAdapter(entry -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("取消拉黑")
                    .setMessage("确定要取消拉黑 " + entry.user.getNickname() + " 吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        apiService.toggleBlock(entry.user.getId()).enqueue(
                                new Callback<ApiService.ApiResponse<Map<String, Object>>>() {
                            @Override
                            public void onResponse(Call<ApiService.ApiResponse<Map<String, Object>>> call,
                                                   Response<ApiService.ApiResponse<Map<String, Object>>> response) {
                                blacklist.remove(entry);
                                adapter.notifyDataSetChanged();
                                updateEmptyState();
                                Toast.makeText(BlacklistManageActivity.this, "已取消拉黑", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<ApiService.ApiResponse<Map<String, Object>>> call, Throwable t) {
                                Toast.makeText(BlacklistManageActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
        binding.rvBlacklist.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBlacklist.setAdapter(adapter);

        loadBlacklist();
    }

    private void loadBlacklist() {
        apiService.getBlocklist().enqueue(new Callback<ApiService.ApiResponse<List<ApiService.WorldData>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<ApiService.WorldData>>> call,
                                   Response<ApiService.ApiResponse<List<ApiService.WorldData>>> response) {
                blacklist.clear();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (ApiService.WorldData wd : response.body().data) {
                        User u = new User();
                        u.setId(wd.userId);
                        u.setNickname(wd.nickname);
                        u.setUsername(wd.username);
                        u.setAvatar(wd.avatarUrl);
                        BlockEntry entry = new BlockEntry();
                        entry.user = u;
                        blacklist.add(entry);
                    }
                }
                adapter.setEntries(blacklist);
                updateEmptyState();
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<ApiService.WorldData>>> call, Throwable t) {
                Toast.makeText(BlacklistManageActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (blacklist.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvBlacklist.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvBlacklist.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private static class BlacklistAdapter extends RecyclerView.Adapter<BlacklistAdapter.ViewHolder> {

        private List<BlockEntry> entries = new ArrayList<>();
        private final OnUnblockListener listener;

        interface OnUnblockListener { void onUnblock(BlockEntry entry); }

        BlacklistAdapter(OnUnblockListener listener) { this.listener = listener; }

        void setEntries(List<BlockEntry> entries) {
            this.entries = entries != null ? entries : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(entries.get(position));
        }

        @Override
        public int getItemCount() { return entries.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            CircleImageView ivAvatar;
            TextView tvText, tvTime;

            ViewHolder(View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.iv_avatar);
                tvText = itemView.findViewById(R.id.tv_notification_text);
                tvTime = itemView.findViewById(R.id.tv_notification_time);
            }

            void bind(BlockEntry entry) {
                tvText.setText(entry.user.getNickname());
                tvTime.setText("点击取消拉黑");
                ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onUnblock(entry);
                });
            }
        }
    }
}
