package com.suisui.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.suisui.app.R;
import com.suisui.app.model.Moment;
import com.suisui.app.util.ImageLoader;
import com.suisui.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 碎碎念卡片列表适配器
 */
public class MomentAdapter extends RecyclerView.Adapter<MomentAdapter.ViewHolder> {

    private List<Moment> moments = new ArrayList<>();
    private OnMomentClickListener listener;
    private boolean isOwnWorld = true;

    public interface OnMomentClickListener {
        void onMomentClick(Moment moment, int position);
        void onLikeClick(Moment moment, int position);
        void onCommentClick(Moment moment, int position);
        void onDeleteClick(Moment moment, int position);
        void onImageClick(Moment moment, int imageIndex);
    }

    public MomentAdapter(OnMomentClickListener listener) {
        this.listener = listener;
    }

    public void setOwnWorld(boolean ownWorld) {
        this.isOwnWorld = ownWorld;
    }

    public void setMoments(List<Moment> moments) {
        this.moments = moments != null ? moments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addMoment(Moment moment) {
        this.moments.add(0, moment);
        notifyItemInserted(0);
    }

    public void removeMoment(int position) {
        if (position >= 0 && position < moments.size()) {
            moments.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateMomentLike(int position, boolean isLiked, int likeCount) {
        if (position >= 0 && position < moments.size()) {
            Moment m = moments.get(position);
            m.setLiked(isLiked);
            m.setLikeCount(likeCount);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_moment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Moment moment = moments.get(position);
        holder.bind(moment, position);
    }

    @Override
    public int getItemCount() {
        return moments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserNickname, tvTime, tvContent, tvViewAll;
        TextView tvTag1, tvTag2;
        ImageView ivLike;
        TextView tvLikeCount, tvCommentCount;
        ImageButton btnDelete;
        RecyclerView rvImages;
        LinearLayout layoutTags, btnLike, btnComment;
        ImageThumbnailAdapter imageAdapter;

        ViewHolder(View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserNickname = itemView.findViewById(R.id.tv_user_nickname);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvViewAll = itemView.findViewById(R.id.tv_view_all);
            tvTag1 = itemView.findViewById(R.id.tv_tag1);
            tvTag2 = itemView.findViewById(R.id.tv_tag2);
            ivLike = itemView.findViewById(R.id.iv_like);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            rvImages = itemView.findViewById(R.id.rv_images);
            layoutTags = itemView.findViewById(R.id.layout_tags);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);

            rvImages.setLayoutManager(new GridLayoutManager(itemView.getContext(), 3));
            imageAdapter = new ImageThumbnailAdapter();
            rvImages.setAdapter(imageAdapter);
        }

        void bind(Moment moment, int position) {
            // 用户头像
            ImageLoader.loadAvatar(itemView.getContext(), moment.getUserAvatar(), ivUserAvatar);

            // 用户信息（如果设置了的话）
            if (moment.getUserNickname() != null && !moment.getUserNickname().isEmpty()) {
                tvUserNickname.setText(moment.getUserNickname());
            } else {
                tvUserNickname.setText("用户");
            }

            // 时间
            tvTime.setText(TimeUtils.formatFriendly(moment.getCreatedAt()));

            // 文字内容
            tvContent.setText(moment.getContent());

            // 是否折叠
            if (moment.isTextOverflow()) {
                tvContent.setMaxLines(5);
                tvViewAll.setVisibility(View.VISIBLE);
            } else {
                tvContent.setMaxLines(Integer.MAX_VALUE);
                tvViewAll.setVisibility(View.GONE);
            }

            // 图片
            if (moment.hasImages()) {
                rvImages.setVisibility(View.VISIBLE);
                imageAdapter.setImages(moment.getImages(), index -> {
                    if (listener != null) listener.onImageClick(moment, index);
                });
            } else {
                rvImages.setVisibility(View.GONE);
            }

            // 标签
            List<String> tags = moment.getTags();
            if (tags != null && !tags.isEmpty()) {
                layoutTags.setVisibility(View.VISIBLE);
                tvTag1.setText(tags.get(0));
                tvTag1.setVisibility(View.VISIBLE);
                if (tags.size() > 1) {
                    tvTag2.setText(tags.get(1));
                    tvTag2.setVisibility(View.VISIBLE);
                } else {
                    tvTag2.setVisibility(View.GONE);
                }
            } else {
                layoutTags.setVisibility(View.GONE);
            }

            // 点赞状态
            if (moment.isLiked()) {
                ivLike.setImageResource(R.drawable.ic_like_filled);
            } else {
                ivLike.setImageResource(R.drawable.ic_like);
            }
            tvLikeCount.setText(String.valueOf(moment.getLikeCount()));
            tvCommentCount.setText(String.valueOf(moment.getCommentCount()));

            // 删除按钮（仅自己的卡片可见）
            btnDelete.setVisibility(isOwnWorld ? View.VISIBLE : View.GONE);

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onMomentClick(moment, position);
            });

            btnLike.setOnClickListener(v -> {
                if (listener != null) listener.onLikeClick(moment, position);
            });

            btnComment.setOnClickListener(v -> {
                if (listener != null) listener.onCommentClick(moment, position);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(moment, position);
            });

            tvViewAll.setOnClickListener(v -> {
                if (listener != null) listener.onMomentClick(moment, position);
            });
        }
    }

    /**
     * 图片缩略图小适配器
     */
    private static class ImageThumbnailAdapter extends RecyclerView.Adapter<ImageThumbnailAdapter.ImageHolder> {

        private List<String> images = new ArrayList<>();
        private OnImageClickListener imageClickListener;

        interface OnImageClickListener {
            void onImageClick(int index);
        }

        void setImages(List<String> images, OnImageClickListener listener) {
            this.images = images != null ? images : new ArrayList<>();
            this.imageClickListener = listener;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_thumbnail, parent, false);
            return new ImageHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return Math.min(images.size(), 3);
        }

        class ImageHolder extends RecyclerView.ViewHolder {
            ImageView ivThumbnail;

            ImageHolder(View itemView) {
                super(itemView);
                ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            }

            void bind(int position) {
                String imageUrl = images.get(position);
                ImageLoader.loadThumbnail(ivThumbnail.getContext(), imageUrl, ivThumbnail);
                ivThumbnail.setOnClickListener(v -> {
                    if (imageClickListener != null) imageClickListener.onImageClick(position);
                });
            }
        }
    }
}
