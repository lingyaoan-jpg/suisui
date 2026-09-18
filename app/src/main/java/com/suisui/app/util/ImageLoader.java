package com.suisui.app.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.suisui.app.R;
import com.suisui.app.api.ApiClient;

/**
 * 图片加载工具类
 * 统一处理头像、背景图、缩略图的 Glide 加载逻辑
 */
public class ImageLoader {

    // 后端服务地址：统一复用 ApiClient 的配置，避免两处不一致
    public static final String BASE_URL = trimTrailingSlash(ApiClient.BASE_URL);

    // 注意：布局使用 CircleImageView，自带圆形裁剪，不再叠加 CircleCrop
    private static final RequestOptions AVATAR_OPTIONS = new RequestOptions()
            .placeholder(R.drawable.bg_avatar_placeholder)
            .error(R.drawable.bg_avatar_placeholder)
            .dontTransform();

    private static final RequestOptions BG_OPTIONS = new RequestOptions()
            .placeholder(R.drawable.bg_avatar_placeholder)
            .error(R.drawable.bg_avatar_placeholder)
            .centerCrop();

    private static final RequestOptions THUMBNAIL_OPTIONS = new RequestOptions()
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .centerCrop()
            .transform(new RoundedCorners(8));

    /**
     * 加载圆形头像
     * @param url 相对路径（如 /api/images/xxx.jpg）或完整 URL
     */
    public static void loadAvatar(Context context, String url, ImageView imageView) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.drawable.bg_avatar_placeholder);
            return;
        }
        String fullUrl = resolveUrl(url);
        Glide.with(context)
                .load(fullUrl)
                .apply(AVATAR_OPTIONS)
                .into(imageView);
    }

    /**
     * 加载背景图（centerCrop，无圆角）
     */
    public static void loadBackground(Context context, String url, ImageView imageView) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.drawable.bg_avatar_placeholder);
            return;
        }
        String fullUrl = resolveUrl(url);
        Glide.with(context)
                .load(fullUrl)
                .apply(BG_OPTIONS)
                .into(imageView);
    }

    /**
     * 加载缩略图（带圆角）
     */
    public static void loadThumbnail(Context context, String url, ImageView imageView) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.mipmap.ic_launcher);
            return;
        }
        String fullUrl = resolveUrl(url);
        Glide.with(context)
                .load(fullUrl)
                .apply(THUMBNAIL_OPTIONS)
                .into(imageView);
    }

    /**
     * 加载全尺寸图片（适配 ViewPager2 浏览）
     */
    public static void loadImage(Context context, String url, ImageView imageView) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.mipmap.ic_launcher);
            return;
        }
        String fullUrl = resolveUrl(url);
        Glide.with(context)
                .load(fullUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageView);
    }

    private static String trimTrailingSlash(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * 将相对路径拼接为完整 URL，同时兼容 content:// 等本地 URI
     */
    private static String resolveUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")
                || url.startsWith("content://") || url.startsWith("file://")) {
            return url;
        }
        return BASE_URL + url;
    }
}
