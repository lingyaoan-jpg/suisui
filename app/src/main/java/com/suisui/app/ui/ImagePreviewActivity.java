package com.suisui.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.suisui.app.adapter.ImagePagerAdapter;
import com.suisui.app.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 全屏图片预览 - 从任意页面传入图片列表和起始位置
 */
public class ImagePreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> images = getIntent().getStringArrayListExtra("images");
        int position = getIntent().getIntExtra("position", 0);

        if (images == null || images.isEmpty()) {
            finish();
            return;
        }

        ViewPager2 viewPager = new ViewPager2(this);
        viewPager.setLayoutParams(new ViewPager2.LayoutParams(
                ViewPager2.LayoutParams.MATCH_PARENT, ViewPager2.LayoutParams.MATCH_PARENT));
        viewPager.setBackgroundColor(0xFF000000);
        setContentView(viewPager);

        ImagePagerAdapter adapter = new ImagePagerAdapter(this);
        adapter.setImages(images);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position, false);

        // 点击关闭
        viewPager.setOnClickListener(v -> finish());
    }
}
