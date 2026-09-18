package com.suisui.app.ui.me;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.suisui.app.R;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.api.SessionManager;
import com.suisui.app.databinding.ActivityEditProfileBinding;
import com.suisui.app.util.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private ApiService apiService;
    private SessionManager sessionManager;

    private String currentAvatarUrl;
    private String currentBgUrl;

    private boolean pickingAvatar = true;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadImage(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance(this).getService();
        sessionManager = SessionManager.getInstance(this);

        initViews();
        loadProfile();
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());

        String savedAvatar = sessionManager.getAvatarUrl();
        if (savedAvatar != null && !savedAvatar.isEmpty()) {
            currentAvatarUrl = savedAvatar;
            ImageLoader.loadAvatar(this, savedAvatar, binding.ivAvatar);
        }

        binding.ivAvatar.setOnClickListener(v -> {
            pickingAvatar = true;
            imagePickerLauncher.launch("image/*");
        });

        binding.ivBackground.setOnClickListener(v -> {
            pickingAvatar = false;
            imagePickerLauncher.launch("image/*");
        });

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        long userId = sessionManager.getUserId();
        apiService.getWorld(userId).enqueue(new Callback<ApiService.ApiResponse<ApiService.WorldData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.WorldData>> call,
                                   Response<ApiService.ApiResponse<ApiService.WorldData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiService.WorldData world = response.body().data;
                    binding.etNickname.setText(world.nickname != null ? world.nickname : "");
                    binding.etBio.setText(world.bio != null ? world.bio : "");
                    currentAvatarUrl = world.avatarUrl;
                    currentBgUrl = world.bgUrl;
                    ImageLoader.loadAvatar(EditProfileActivity.this, world.avatarUrl, binding.ivAvatar);
                    ImageLoader.loadBackground(EditProfileActivity.this, world.bgUrl, binding.ivBackground);
                    sessionManager.saveAvatarUrl(world.avatarUrl != null ? world.avatarUrl : "");
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.WorldData>> call, Throwable t) {
                binding.etNickname.setText(sessionManager.getNickname());
            }
        });
    }

    private void uploadImage(Uri uri) {
        try {
            String fileName = getFileName(uri);
            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "image/jpeg";

            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(this, "无法读取图片", Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            inputStream.close();

            RequestBody requestFile = RequestBody.create(bos.toByteArray(), MediaType.parse(mimeType));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileName, requestFile);

            apiService.uploadImage(part).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Object data = response.body().get("data");
                        String url = data != null ? data.toString() : uri.toString();
                        if (pickingAvatar) {
                            currentAvatarUrl = url;
                            ImageLoader.loadAvatar(EditProfileActivity.this, url, binding.ivAvatar);
                        } else {
                            currentBgUrl = url;
                            ImageLoader.loadBackground(EditProfileActivity.this, url, binding.ivBackground);
                        }
                        Toast.makeText(EditProfileActivity.this, "图片上传成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "上传失败，使用本地图片", Toast.LENGTH_SHORT).show();
                        if (pickingAvatar) {
                            currentAvatarUrl = uri.toString();
                            ImageLoader.loadAvatar(EditProfileActivity.this, uri.toString(), binding.ivAvatar);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    // 上传失败时使用本地URI
                    if (pickingAvatar) {
                        currentAvatarUrl = uri.toString();
                        ImageLoader.loadAvatar(EditProfileActivity.this, uri.toString(), binding.ivAvatar);
                    }
                    Toast.makeText(EditProfileActivity.this, "上传失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "读取图片出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String name = "image.jpg";
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        }
        return name;
    }

    private void saveProfile() {
        String nickname = binding.etNickname.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();

        if (nickname.isEmpty()) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("保存中...");

        Map<String, String> body = new HashMap<>();
        body.put("nickname", nickname);
        body.put("bio", bio);
        if (currentAvatarUrl != null) body.put("avatarUrl", currentAvatarUrl);
        if (currentBgUrl != null) body.put("bgUrl", currentBgUrl);

        apiService.updateProfile(body).enqueue(new Callback<ApiService.ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Void>> call,
                                   Response<ApiService.ApiResponse<Void>> response) {
                binding.btnSave.setEnabled(true);
                binding.btnSave.setText("保存");
                if (response.isSuccessful() && response.body() != null && response.body().code == 200) {
                    sessionManager.saveNickname(nickname);
                    if (currentAvatarUrl != null) sessionManager.saveAvatarUrl(currentAvatarUrl);
                    Toast.makeText(EditProfileActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().message : "保存失败";
                    Toast.makeText(EditProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Void>> call, Throwable t) {
                binding.btnSave.setEnabled(true);
                binding.btnSave.setText("保存");
                Toast.makeText(EditProfileActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
