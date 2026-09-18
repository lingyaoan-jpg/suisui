package com.suisui.app;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.suisui.app.databinding.ActivityMainBinding;
import com.suisui.app.ui.discover.DiscoverFragment;
import com.suisui.app.ui.me.MeFragment;
import com.suisui.app.ui.world.MyWorldFragment;

/**
 * 主Activity - 底部三Tab导航
 * 小世界 | 发现 | 我的
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MyWorldFragment myWorldFragment;
    private DiscoverFragment discoverFragment;
    private MeFragment meFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFragments();
        setupBottomNav();
        setupKeyboardListener();

        // 默认显示小世界
        if (savedInstanceState == null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_world);
        }
    }

    private void initFragments() {
        myWorldFragment = new MyWorldFragment();
        discoverFragment = new DiscoverFragment();
        meFragment = new MeFragment();
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_world) {
                    switchFragment(myWorldFragment);
                    return true;
                } else if (itemId == R.id.nav_discover) {
                    switchFragment(discoverFragment);
                    return true;
                } else if (itemId == R.id.nav_me) {
                    switchFragment(meFragment);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 键盘弹起时隐藏底部导航栏，收起时恢复
     */
    private void setupKeyboardListener() {
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            binding.getRoot().getWindowVisibleDisplayFrame(r);
            int screenHeight = binding.getRoot().getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                if (binding.bottomNav.getVisibility() != View.GONE) {
                    binding.bottomNav.setVisibility(View.GONE);
                }
            } else {
                if (binding.bottomNav.getVisibility() != View.VISIBLE) {
                    binding.bottomNav.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * 导航到他人小世界页面
     */
    public void navigateToOtherWorld(long userId, String nickname) {
        Intent intent = new Intent(this, com.suisui.app.ui.discover.OtherWorldActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("nickname", nickname);
        startActivity(intent);
    }

    public MyWorldFragment getMyWorldFragment() {
        return myWorldFragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
