package com.suisui.app.ui.world;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.suisui.app.R;
import com.suisui.app.adapter.CommentAdapter;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.api.SessionManager;
import com.suisui.app.databinding.ActivityMomentDetailBinding;
import com.suisui.app.model.Comment;
import com.suisui.app.model.Moment;
import com.suisui.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MomentDetailActivity extends AppCompatActivity {

    private ActivityMomentDetailBinding binding;
    private CommentAdapter commentAdapter;
    private ApiService apiService;
    private List<Comment> comments = new ArrayList<>();
    private Moment currentMoment;
    private long momentId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMomentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance(this).getService();

        momentId = getIntent().getLongExtra("moment_id", -1);
        boolean focusComment = getIntent().getBooleanExtra("focus_comment", false);

        initViews();
        loadMoment();
        loadComments();

        if (focusComment) {
            binding.etComment.requestFocus();
        }
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());

        commentAdapter = new CommentAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);

        binding.btnLike.setOnClickListener(v -> toggleLike());
        binding.btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void loadMoment() {
        apiService.getNoteDetail(momentId).enqueue(new Callback<ApiService.ApiResponse<ApiService.NoteData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.NoteData>> call,
                                   Response<ApiService.ApiResponse<ApiService.NoteData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiService.NoteData note = response.body().data;
                    currentMoment = new Moment();
                    currentMoment.setId(note.id);
                    currentMoment.setUserId(note.userId);
                    currentMoment.setContent(note.content);
                    currentMoment.setImages(note.images);
                    currentMoment.setTags(note.tags);
                    currentMoment.setLikeCount(note.likeCount);
                    currentMoment.setCommentCount(note.commentCount);
                    currentMoment.setLiked(note.isLiked);
                    currentMoment.setUserNickname(note.userNickname);
                    currentMoment.setUserAvatar(note.userAvatar);
                    currentMoment.setCreatedAt(com.suisui.app.util.TimeUtils.parseServerDate(note.createdAt));
                    displayMoment(currentMoment);
                } else {
                    Toast.makeText(MomentDetailActivity.this, "碎碎念不存在", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.NoteData>> call, Throwable t) {
                Toast.makeText(MomentDetailActivity.this, "加载失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayMoment(Moment moment) {
        binding.tvNickname.setText(moment.getUserNickname() != null ? moment.getUserNickname() : "用户");
        com.suisui.app.util.ImageLoader.loadAvatar(
                MomentDetailActivity.this, moment.getUserAvatar(), binding.ivAvatar);
        binding.tvTime.setText(TimeUtils.formatFriendly(moment.getCreatedAt()));
        binding.tvContent.setText(moment.getContent());
        binding.tvLikeCount.setText(String.valueOf(moment.getLikeCount()));
        binding.tvCommentCount.setText("条评论");

        updateLikeUI();

        // 加载图片 ViewPager（自适应高度）
        if (moment.hasImages()) {
            try {
                binding.vpImages.setVisibility(View.VISIBLE);
                binding.vpImages.setNestedScrollingEnabled(false);

                // 单图放大，多图缩小
                int imgCount = moment.getImages().size();
                ViewGroup.LayoutParams lp = binding.vpImages.getLayoutParams();
                lp.height = imgCount == 1
                        ? (int) (400 * getResources().getDisplayMetrics().density)
                        : (int) (250 * getResources().getDisplayMetrics().density);
                binding.vpImages.setLayoutParams(lp);

                com.suisui.app.adapter.ImagePagerAdapter imageAdapter =
                        new com.suisui.app.adapter.ImagePagerAdapter(this);
                imageAdapter.setImages(moment.getImages());
                binding.vpImages.setAdapter(imageAdapter);

                // 点击图片全屏预览
                final List<String> imageList = moment.getImages();
                final int clickPos = getIntent().getIntExtra("focus_image", 0);
                if (clickPos > 0 && clickPos < imageList.size()) {
                    binding.vpImages.setCurrentItem(clickPos, false);
                }

                binding.vpImages.setOnClickListener(v -> {
                    Intent intent = new Intent(MomentDetailActivity.this,
                            com.suisui.app.ui.ImagePreviewActivity.class);
                    intent.putStringArrayListExtra("images", new ArrayList<>(imageList));
                    intent.putExtra("position", binding.vpImages.getCurrentItem());
                    startActivity(intent);
                });

            // 图片指示器
            if (moment.getImages().size() > 1) {
                binding.layoutImageIndicator.setVisibility(View.VISIBLE);
                binding.layoutImageIndicator.removeAllViews();
                for (int i = 0; i < moment.getImages().size(); i++) {
                    View dot = new View(this);
                    int size = (int) (6 * getResources().getDisplayMetrics().density);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    params.setMargins(4, 0, 4, 0);
                    dot.setLayoutParams(params);
                    dot.setBackgroundResource(i == 0
                            ? R.drawable.bg_tag_chip_selected
                            : R.drawable.bg_tag_chip);
                    binding.layoutImageIndicator.addView(dot);
                }
                binding.vpImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        for (int i = 0; i < binding.layoutImageIndicator.getChildCount(); i++) {
                            View dot = binding.layoutImageIndicator.getChildAt(i);
                            dot.setBackgroundResource(i == position
                                    ? R.drawable.bg_tag_chip_selected
                                    : R.drawable.bg_tag_chip);
                        }
                    }
                });
            } else {
                binding.layoutImageIndicator.setVisibility(View.GONE);
            }
            } catch (Exception e) {
                binding.vpImages.setVisibility(View.GONE);
                binding.layoutImageIndicator.setVisibility(View.GONE);
            }
        } else {
            binding.vpImages.setVisibility(View.GONE);
            binding.layoutImageIndicator.setVisibility(View.GONE);
        }

        if (moment.getTags() != null && !moment.getTags().isEmpty()) {
            binding.layoutTags.setVisibility(View.VISIBLE);
            binding.layoutTags.removeAllViews();
            for (String tag : moment.getTags()) {
                TextView tagView = (TextView) getLayoutInflater()
                        .inflate(R.layout.item_tag_chip, binding.layoutTags, false);
                tagView.setText(tag);
                binding.layoutTags.addView(tagView);
            }
        }
    }

    private void loadComments() {
        apiService.getComments(momentId, 1, 100).enqueue(
                new Callback<ApiService.ApiResponse<ApiService.PageData<ApiService.CommentData>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.PageData<ApiService.CommentData>>> call,
                                   Response<ApiService.ApiResponse<ApiService.PageData<ApiService.CommentData>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    comments.clear();
                    for (ApiService.CommentData cd : response.body().data.content) {
                        Comment c = new Comment();
                        c.setId(cd.id);
                        c.setMomentId(cd.noteId);
                        c.setUserId(cd.userId);
                        c.setUsername(cd.username);
                        c.setUserAvatar(cd.userAvatar);
                        c.setContent(cd.content);
                        c.setCreatedAt(com.suisui.app.util.TimeUtils.parseServerDate(cd.createdAt));
                        comments.add(c);
                    }
                    commentAdapter.setComments(comments);
                }

                if (comments.isEmpty()) {
                    binding.tvNoComments.setVisibility(View.VISIBLE);
                    binding.rvComments.setVisibility(View.GONE);
                } else {
                    binding.tvNoComments.setVisibility(View.GONE);
                    binding.rvComments.setVisibility(View.VISIBLE);
                }
                binding.tvCommentCount.setText(comments.size() + " 条评论");
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.PageData<ApiService.CommentData>>> call, Throwable t) {
                Toast.makeText(MomentDetailActivity.this, "加载评论失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLike() {
        if (currentMoment == null) return;
        Map<String, Long> body = new HashMap<>();
        body.put("noteId", currentMoment.getId());

        apiService.toggleLike(body).enqueue(new Callback<ApiService.ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Map<String, Object>>> call,
                                   Response<ApiService.ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    Map<String, Object> data = response.body().data;
                    boolean isLiked = data.get("isLiked") != null && (boolean) data.get("isLiked");
                    int likeCount = data.get("likeCount") != null ? ((Number) data.get("likeCount")).intValue() : 0;
                    currentMoment.setLiked(isLiked);
                    currentMoment.setLikeCount(likeCount);
                    binding.tvLikeCount.setText(String.valueOf(likeCount));
                    updateLikeUI();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(MomentDetailActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeUI() {
        if (currentMoment != null && currentMoment.isLiked()) {
            binding.ivLike.setImageResource(R.drawable.ic_like_filled);
        } else {
            binding.ivLike.setImageResource(R.drawable.ic_like);
        }
    }

    private void sendComment() {
        if (currentMoment == null) return;
        String content = binding.etComment.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("noteId", currentMoment.getId());
        body.put("content", content);

        apiService.createComment(body).enqueue(new Callback<ApiService.ApiResponse<ApiService.CommentData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.CommentData>> call,
                                   Response<ApiService.ApiResponse<ApiService.CommentData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiService.CommentData cd = response.body().data;
                    Comment c = new Comment();
                    c.setId(cd.id);
                    c.setMomentId(cd.noteId);
                    c.setUserId(cd.userId);
                    c.setUsername(cd.username);
                    c.setUserAvatar(cd.userAvatar);
                    c.setContent(cd.content);
                    c.setCreatedAt(System.currentTimeMillis());
                    commentAdapter.addComment(c);
                    comments.add(c);

                    currentMoment.setCommentCount(currentMoment.getCommentCount() + 1);
                    binding.tvCommentCount.setText(comments.size() + " 条评论");
                    binding.tvNoComments.setVisibility(View.GONE);
                    binding.rvComments.setVisibility(View.VISIBLE);
                    binding.etComment.setText("");
                    Toast.makeText(MomentDetailActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.CommentData>> call, Throwable t) {
                Toast.makeText(MomentDetailActivity.this, "评论失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
