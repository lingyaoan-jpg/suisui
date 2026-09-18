package com.suisui.app.ui.world;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.suisui.app.R;
import com.suisui.app.adapter.MomentAdapter;
import com.suisui.app.adapter.TagAdapter;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.api.SessionManager;
import com.suisui.app.databinding.FragmentMyWorldBinding;
import com.suisui.app.model.Moment;
import com.suisui.app.util.DataGenerator;
import com.suisui.app.util.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyWorldFragment extends Fragment {

    private FragmentMyWorldBinding binding;
    private MomentAdapter momentAdapter;
    private TagAdapter tagAdapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private long currentUserId;

    private List<Moment> allMoments = new ArrayList<>();
    private List<Moment> filteredMoments = new ArrayList<>();
    private String selectedTag = null;
    private List<String> pendingImages = new ArrayList<>();
    private List<String> pendingTags = new ArrayList<>();

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    addPendingImage(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMyWorldBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getInstance(requireContext()).getService();
        sessionManager = SessionManager.getInstance(requireContext());
        currentUserId = sessionManager.getUserId();

        initViews();
        setupAdapters();
        loadUserInfo();
        loadMoments();
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 从编辑资料页返回后刷新
        loadUserInfo();
    }

    private void initViews() {
        binding.tvNickname.setText(sessionManager.getNickname());
        binding.tvBio.setText("");
        binding.tvSubscriberCount.setText("0 订阅者");
        binding.btnAction.setText(R.string.edit_profile);
        binding.btnEditBg.setVisibility(View.GONE);
        String savedAvatar = sessionManager.getAvatarUrl();
        if (savedAvatar != null && !savedAvatar.isEmpty()) {
            ImageLoader.loadAvatar(requireContext(), savedAvatar, binding.ivAvatar);
        }
    }

    private void loadUserInfo() {
        apiService.getWorld(currentUserId).enqueue(new Callback<ApiService.ApiResponse<ApiService.WorldData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.WorldData>> call,
                                   Response<ApiService.ApiResponse<ApiService.WorldData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiService.WorldData world = response.body().data;
                    binding.tvNickname.setText(world.nickname != null ? world.nickname : sessionManager.getNickname());
                    binding.tvBio.setText(world.bio != null ? world.bio : "");
                    binding.tvSubscriberCount.setText(world.subscriberCount + " 订阅者");
                    if (world.avatarUrl != null && !world.avatarUrl.isEmpty()) {
                        ImageLoader.loadAvatar(requireContext(), world.avatarUrl, binding.ivAvatar);
                        sessionManager.saveAvatarUrl(world.avatarUrl);
                    }
                    if (world.bgUrl != null && !world.bgUrl.isEmpty()) {
                        ImageLoader.loadBackground(requireContext(), world.bgUrl, binding.ivBackground);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.WorldData>> call, Throwable t) {
                // 静默失败
            }
        });
    }

    private void setupAdapters() {
        momentAdapter = new MomentAdapter(new MomentAdapter.OnMomentClickListener() {
            @Override
            public void onMomentClick(Moment moment, int position) {
                Intent intent = new Intent(requireContext(), MomentDetailActivity.class);
                intent.putExtra("moment_id", moment.getId());
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Moment moment, int position) {
                toggleLike(moment, position);
            }

            @Override
            public void onCommentClick(Moment moment, int position) {
                Intent intent = new Intent(requireContext(), MomentDetailActivity.class);
                intent.putExtra("moment_id", moment.getId());
                intent.putExtra("focus_comment", true);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Moment moment, int position) {
                showDeleteConfirmDialog(moment, position);
            }

            @Override
            public void onImageClick(Moment moment, int imageIndex) {
                // 直接预览大图，不跳转详情
                Intent intent = new Intent(requireContext(),
                        com.suisui.app.ui.ImagePreviewActivity.class);
                intent.putStringArrayListExtra("images",
                        new ArrayList<>(moment.getImages()));
                intent.putExtra("position", imageIndex);
                startActivity(intent);
            }
        });
        binding.rvMoments.setAdapter(momentAdapter);

        tagAdapter = new TagAdapter((tag, position) -> {
            selectedTag = tag;
            filterMoments();
        });
        binding.rvTagFilter.setAdapter(tagAdapter);
    }

    private void loadMoments() {
        apiService.getUserNotes(currentUserId, 1, 50, selectedTag).enqueue(
                new Callback<ApiService.ApiResponse<ApiService.PageData<ApiService.NoteData>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.PageData<ApiService.NoteData>>> call,
                                   Response<ApiService.ApiResponse<ApiService.PageData<ApiService.NoteData>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<ApiService.NoteData> notes = response.body().data.content;
                    allMoments.clear();
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
                        allMoments.add(m);
                    }
                    filterMoments();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.PageData<ApiService.NoteData>>> call, Throwable t) {
                Toast.makeText(requireContext(), "加载失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 标签列表仍用本地预设
        List<String> allTags = DataGenerator.getAllTags();
        tagAdapter.setTags(allTags);
    }

    private void filterMoments() {
        if (selectedTag == null) {
            filteredMoments = new ArrayList<>(allMoments);
        } else {
            filteredMoments = allMoments.stream()
                    .filter(m -> m.getTags() != null && m.getTags().contains(selectedTag))
                    .collect(Collectors.toList());
        }
        momentAdapter.setMoments(filteredMoments);
    }

    private void setupClickListeners() {
        binding.btnAction.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(),
                    com.suisui.app.ui.me.EditProfileActivity.class);
            startActivity(intent);
        });

        binding.btnSend.setOnClickListener(v -> publishMoment());

        binding.btnTag.setOnClickListener(v -> showTagSelectDialog());

        binding.btnAddImage.setOnClickListener(v -> {
            if (pendingImages.size() >= 3) {
                Toast.makeText(requireContext(), R.string.max_images_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            imagePickerLauncher.launch("image/*");
        });

        binding.btnFilter.setOnClickListener(v -> {
            if (binding.layoutTagFilter.getVisibility() == View.VISIBLE) {
                binding.layoutTagFilter.setVisibility(View.GONE);
            } else {
                binding.layoutTagFilter.setVisibility(View.VISIBLE);
            }
        });

    }

    private void addPendingImage(Uri uri) {
        pendingImages.add(uri.toString());
        refreshImagePreviews();
    }

    private void refreshImagePreviews() {
        binding.containerPreviewImages.removeAllViews();

        if (pendingImages.isEmpty()) {
            binding.layoutImagePreview.setVisibility(View.GONE);
            return;
        }

        binding.layoutImagePreview.setVisibility(View.VISIBLE);
        int thumbSize = (int) (80 * requireContext().getResources().getDisplayMetrics().density);

        for (int i = 0; i < pendingImages.size(); i++) {
            final int index = i;
            String imageUri = pendingImages.get(i);

            FrameLayout frame = new FrameLayout(requireContext());
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(thumbSize, thumbSize);
            frameParams.setMargins(0, 0, 8, 0);
            frame.setLayoutParams(frameParams);

            ImageView thumb = new ImageView(requireContext());
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ImageLoader.loadThumbnail(requireContext(), imageUri, thumb);
            frame.addView(thumb);

            ImageView btnRemove = new ImageView(requireContext());
            btnRemove.setImageResource(R.drawable.ic_delete);
            FrameLayout.LayoutParams removeParams = new FrameLayout.LayoutParams(
                    (int) (20 * requireContext().getResources().getDisplayMetrics().density),
                    (int) (20 * requireContext().getResources().getDisplayMetrics().density));
            removeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
            removeParams.setMargins(0, 4, 4, 0);
            btnRemove.setLayoutParams(removeParams);
            btnRemove.setBackgroundResource(R.drawable.bg_card);
            btnRemove.setOnClickListener(v -> {
                pendingImages.remove(index);
                refreshImagePreviews();
            });
            frame.addView(btnRemove);

            binding.containerPreviewImages.addView(frame);
        }
    }

    private void publishMoment() {
        String content = binding.etInput.getText().toString().trim();
        if (content.isEmpty() && pendingImages.isEmpty()) {
            Toast.makeText(requireContext(), "请输入内容或添加图片", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSend.setEnabled(false);

        // 先上传图片
        if (!pendingImages.isEmpty()) {
            uploadImagesThenPublish(content);
        } else {
            doPublishMoment(content, new ArrayList<>());
        }
    }

    private void uploadImagesThenPublish(String content) {
        List<String> uploadedUrls = new ArrayList<>();
        final int total = pendingImages.size();

        if (total == 0) {
            doPublishMoment(content, uploadedUrls);
            return;
        }

        final int[] completed = {0};

        for (String imageUri : pendingImages) {
            new Thread(() -> {
                try {
                    Uri uri = Uri.parse(imageUri);
                    android.content.ContentResolver resolver = requireContext().getContentResolver();
                    String mimeType = resolver.getType(uri);
                    if (mimeType == null) mimeType = "image/jpeg";

                    java.io.InputStream is = resolver.openInputStream(uri);
                    if (is != null) {
                        byte[] bytes = readBytes(is);
                        is.close();

                        // 写入临时文件
                        java.io.File tempDir = requireContext().getCacheDir();
                        java.io.File tempFile = new java.io.File(tempDir, "upload_" + System.currentTimeMillis() + ".jpg");
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
                        fos.write(bytes);
                        fos.close();

                        RequestBody requestFile = RequestBody.create(tempFile, MediaType.parse(mimeType));
                        MultipartBody.Part part = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

                        apiService.uploadImage(part).enqueue(new Callback<Map<String, Object>>() {
                            @Override
                            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                                synchronized (uploadedUrls) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Object data = response.body().get("data");
                                        if (data != null) uploadedUrls.add(data.toString());
                                    }
                                    completed[0]++;
                                    if (completed[0] >= total) {
                                        requireActivity().runOnUiThread(() -> doPublishMoment(content, uploadedUrls));
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                synchronized (uploadedUrls) {
                                    completed[0]++;
                                    if (completed[0] >= total) {
                                        requireActivity().runOnUiThread(() -> doPublishMoment(content, uploadedUrls));
                                    }
                                }
                            }
                        });
                    } else {
                        synchronized (uploadedUrls) {
                            completed[0]++;
                            if (completed[0] >= total) {
                                requireActivity().runOnUiThread(() -> doPublishMoment(content, uploadedUrls));
                            }
                        }
                    }
                } catch (Exception e) {
                    synchronized (uploadedUrls) {
                        completed[0]++;
                        if (completed[0] >= total) {
                            requireActivity().runOnUiThread(() -> doPublishMoment(content, uploadedUrls));
                        }
                    }
                }
            }).start();
        }
    }

    private byte[] readBytes(java.io.InputStream is) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }

    private void doPublishMoment(String content, List<String> imageUrls) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("images", imageUrls);
        body.put("tags", new ArrayList<>(pendingTags));

        apiService.createNote(body).enqueue(new Callback<ApiService.ApiResponse<ApiService.NoteData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.NoteData>> call,
                                   Response<ApiService.ApiResponse<ApiService.NoteData>> response) {
                binding.btnSend.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiService.NoteData note = response.body().data;
                    Moment newMoment = new Moment();
                    newMoment.setId(note.id);
                    newMoment.setUserId(note.userId);
                    newMoment.setContent(note.content);
                    newMoment.setImages(note.images);
                    newMoment.setTags(note.tags);
                    newMoment.setLikeCount(note.likeCount);
                    newMoment.setCommentCount(note.commentCount);
                    newMoment.setLiked(note.isLiked);
                    newMoment.setUserNickname(note.userNickname);
                    newMoment.setUserAvatar(note.userAvatar);
                    newMoment.setCreatedAt(com.suisui.app.util.TimeUtils.parseServerDate(note.createdAt));
                    allMoments.add(0, newMoment);
                    filterMoments();
                    binding.etInput.setText("");
                    pendingImages.clear();
                    pendingTags.clear();
                    binding.layoutImagePreview.setVisibility(View.GONE);
                    binding.containerPreviewImages.removeAllViews();
                    binding.scrollView.smoothScrollTo(0, 0);
                    Toast.makeText(requireContext(), "发布成功！", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = response.body() != null ? response.body().message : "发布失败";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.NoteData>> call, Throwable t) {
                binding.btnSend.setEnabled(true);
                Toast.makeText(requireContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    boolean isLiked = data.get("isLiked") != null && (boolean) data.get("isLiked");
                    int likeCount = data.get("likeCount") != null ? ((Number) data.get("likeCount")).intValue() : 0;
                    moment.setLiked(isLiked);
                    moment.setLikeCount(likeCount);
                    momentAdapter.updateMomentLike(position, isLiked, likeCount);
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(requireContext(), "操作失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog(Moment moment, int position) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    apiService.deleteNote(moment.getId()).enqueue(new Callback<ApiService.ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse<Void>> call,
                                               Response<ApiService.ApiResponse<Void>> response) {
                            if (response.isSuccessful()) {
                                allMoments.remove(moment);
                                filterMoments();
                                Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiService.ApiResponse<Void>> call, Throwable t) {
                            Toast.makeText(requireContext(), "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showTagSelectDialog() {
        List<String> allTags = DataGenerator.getAllTags();
        String[] tagArray = allTags.toArray(new String[0]);
        boolean[] checkedItems = new boolean[tagArray.length];
        for (int i = 0; i < tagArray.length; i++) {
            checkedItems[i] = pendingTags.contains(tagArray[i]);
        }

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_tag)
                .setMultiChoiceItems(tagArray, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!pendingTags.contains(tagArray[which])) pendingTags.add(tagArray[which]);
                    } else {
                        pendingTags.remove(tagArray[which]);
                    }
                })
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (!pendingTags.isEmpty()) {
                        binding.etInput.setHint("已选标签: " + String.join(", ", pendingTags));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
