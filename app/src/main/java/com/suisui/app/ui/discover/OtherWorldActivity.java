package com.suisui.app.ui.discover;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.suisui.app.R;
import com.suisui.app.adapter.MomentAdapter;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.api.SessionManager;
import com.suisui.app.databinding.ActivityOtherWorldBinding;
import com.suisui.app.model.Moment;
import com.suisui.app.ui.world.MomentDetailActivity;
import com.suisui.app.util.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtherWorldActivity extends AppCompatActivity {

    private ActivityOtherWorldBinding binding;
    private MomentAdapter momentAdapter;
    private ApiService apiService;
    private long targetUserId;
    private boolean isSubscribed = false;
    private boolean isBlocked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtherWorldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance(this).getService();

        targetUserId = getIntent().getLongExtra("user_id", -1);
        String nickname = getIntent().getStringExtra("nickname");

        initViews(nickname);
        loadWorldData();
    }

    private void initViews(String nickname) {
        binding.tvTitle.setText(nickname != null ? nickname + "的小世界" : "小世界");
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnMenu.setOnClickListener(v -> showMenuDialog());
        binding.btnSubscribe.setOnClickListener(v -> toggleSubscribe());

        momentAdapter = new MomentAdapter(new MomentAdapter.OnMomentClickListener() {
            @Override
            public void onMomentClick(Moment moment, int position) {
                Intent intent = new Intent(OtherWorldActivity.this, MomentDetailActivity.class);
                intent.putExtra("moment_id", moment.getId());
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Moment moment, int position) { toggleLike(moment, position); }

            @Override
            public void onCommentClick(Moment moment, int position) {
                Intent intent = new Intent(OtherWorldActivity.this, MomentDetailActivity.class);
                intent.putExtra("moment_id", moment.getId());
                intent.putExtra("focus_comment", true);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Moment moment, int position) {}

            @Override
            public void onImageClick(Moment moment, int imageIndex) {
                Intent intent = new Intent(OtherWorldActivity.this,
                        com.suisui.app.ui.ImagePreviewActivity.class);
                intent.putStringArrayListExtra("images",
                        new ArrayList<>(moment.getImages()));
                intent.putExtra("position", imageIndex);
                startActivity(intent);
            }
        });
        momentAdapter.setOwnWorld(false);
        binding.rvMoments.setAdapter(momentAdapter);
    }

    private void loadWorldData() {
        apiService.getWorld(targetUserId).enqueue(new Callback<ApiService.ApiResponse<ApiService.WorldData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.WorldData>> call,
                                   Response<ApiService.ApiResponse<ApiService.WorldData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiService.WorldData user = response.body().data;
                    binding.tvNickname.setText(user.nickname);
                    binding.tvBio.setText(user.bio != null ? user.bio : "这个人很懒，什么都没写...");
                    binding.tvSubscriberCount.setText(user.subscriberCount + " 订阅者");
                    isSubscribed = user.subscribed;
                    updateSubscribeButton();
                    ImageLoader.loadAvatar(OtherWorldActivity.this, user.avatarUrl, binding.ivAvatar);
                    ImageLoader.loadBackground(OtherWorldActivity.this, user.bgUrl, binding.ivBackground);
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.WorldData>> call, Throwable t) {
                Toast.makeText(OtherWorldActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getUserNotes(targetUserId, 1, 50, null).enqueue(
                new Callback<ApiService.ApiResponse<ApiService.PageData<ApiService.NoteData>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.PageData<ApiService.NoteData>>> call,
                                   Response<ApiService.ApiResponse<ApiService.PageData<ApiService.NoteData>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<ApiService.NoteData> notes = response.body().data.content;
                    List<Moment> moments = new ArrayList<>();
                    for (ApiService.NoteData note : notes) {
                        Moment m = new Moment();
                        m.setId(note.id);
                        m.setUserId(note.userId);
                        m.setContent(note.content);
                        m.setImages(note.images);
                        m.setTags(note.tags);
                        m.setLikeCount(note.likeCount);
                        m.setCommentCount(note.commentCount);
                        m.setLiked(note.isLiked);
                        m.setUserNickname(note.userNickname);
                        m.setUserAvatar(note.userAvatar);
                        m.setCreatedAt(com.suisui.app.util.TimeUtils.parseServerDate(note.createdAt));
                        moments.add(m);
                    }
                    momentAdapter.setMoments(moments);
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.PageData<ApiService.NoteData>>> call, Throwable t) {
                Toast.makeText(OtherWorldActivity.this, "加载碎碎念失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleSubscribe() {
        apiService.toggleSubscription(targetUserId).enqueue(new Callback<ApiService.ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Map<String, Object>>> call,
                                   Response<ApiService.ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    Object subObj = response.body().data.get("subscribed");
                    isSubscribed = subObj != null && (boolean) subObj;
                    updateSubscribeButton();
                    Toast.makeText(OtherWorldActivity.this,
                            isSubscribed ? "已订阅" : "已取消订阅", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(OtherWorldActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLike(Moment moment, int position) {
        Map<String, Long> body = new HashMap<>();
        body.put("noteId", moment.getId());

        apiService.toggleLike(body).enqueue(new Callback<ApiService.ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Map<String, Object>>> call,
                                   Response<ApiService.ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    Map<String, Object> data = response.body().data;
                    boolean liked = data.get("isLiked") != null && (boolean) data.get("isLiked");
                    int likeCount = data.get("likeCount") != null ? ((Number) data.get("likeCount")).intValue() : 0;
                    moment.setLiked(liked);
                    moment.setLikeCount(likeCount);
                    momentAdapter.updateMomentLike(position, liked, likeCount);
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(OtherWorldActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSubscribeButton() {
        Button btn = binding.btnSubscribe;
        if (isSubscribed) {
            btn.setText(R.string.subscribed);
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundResource(R.drawable.bg_button_primary);
        } else {
            btn.setText(R.string.subscribe);
            btn.setTextColor(getResources().getColor(R.color.primary));
            btn.setBackgroundResource(R.drawable.bg_button_outline);
        }
    }

    private void showMenuDialog() {
        String[] items = {isBlocked ? "取消拉黑" : "拉黑该用户"};
        new android.app.AlertDialog.Builder(this)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) toggleBlock();
                })
                .show();
    }

    private void toggleBlock() {
        apiService.toggleBlock(targetUserId).enqueue(new Callback<ApiService.ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Map<String, Object>>> call,
                                   Response<ApiService.ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    Object blockObj = response.body().data.get("blocked");
                    isBlocked = blockObj != null && (boolean) blockObj;
                    Toast.makeText(OtherWorldActivity.this,
                            isBlocked ? "已拉黑" : "已取消拉黑", Toast.LENGTH_SHORT).show();
                    if (isBlocked) finish();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(OtherWorldActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
