package com.suisui.app.ui.me;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.suisui.app.R;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.api.SessionManager;
import com.suisui.app.databinding.ActivityAccountSettingsBinding;
import com.suisui.app.ui.LoginActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountSettingsActivity extends AppCompatActivity {

    private ActivityAccountSettingsBinding binding;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance(this).getService();
        sessionManager = SessionManager.getInstance(this);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        binding.btnLogout.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("退出登录")
                    .setMessage(R.string.logout_confirm)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        sessionManager.clear();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    private void showChangePasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("修改密码");

        final android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final android.widget.EditText etOldPwd = new android.widget.EditText(this);
        etOldPwd.setHint("旧密码");
        etOldPwd.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etOldPwd);

        final android.widget.EditText etNewPwd = new android.widget.EditText(this);
        etNewPwd.setHint("新密码");
        etNewPwd.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 16;
        etNewPwd.setLayoutParams(params);
        layout.addView(etNewPwd);

        builder.setView(layout);
        builder.setPositiveButton("确定", (dialog, which) -> {
            String oldPwd = etOldPwd.getText().toString().trim();
            String newPwd = etNewPwd.getText().toString().trim();

            if (oldPwd.isEmpty() || newPwd.isEmpty()) {
                Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPwd.length() < 6) {
                Toast.makeText(this, "新密码至少6位", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> body = new HashMap<>();
            body.put("oldPassword", oldPwd);
            body.put("newPassword", newPwd);

            apiService.changePassword(body).enqueue(new Callback<ApiService.ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiService.ApiResponse<Void>> call,
                                       Response<ApiService.ApiResponse<Void>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().code == 200) {
                        Toast.makeText(AccountSettingsActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = response.body() != null ? response.body().message : "修改失败";
                        Toast.makeText(AccountSettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiService.ApiResponse<Void>> call, Throwable t) {
                    Toast.makeText(AccountSettingsActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
