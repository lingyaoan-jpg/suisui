package com.suisui.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suisui.app.R;
import com.suisui.app.model.User;
import com.suisui.app.util.ImageLoader;
import com.suisui.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 世界卡片列表适配器（发现页 & 订阅页）
 */
public class WorldCardAdapter extends RecyclerView.Adapter<WorldCardAdapter.ViewHolder> {

    private List<User> users = new ArrayList<>();
    private OnWorldClickListener listener;

    public interface OnWorldClickListener {
        void onWorldClick(User user, int position);
    }

    public WorldCardAdapter(OnWorldClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_world_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position), position);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBackground;
        CircleImageView ivAvatar;
        TextView tvNickname, tvActiveTime, tvSubscribedBadge, tvLatestMoment;

        ViewHolder(View itemView) {
            super(itemView);
            ivBackground = itemView.findViewById(R.id.iv_background);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvActiveTime = itemView.findViewById(R.id.tv_active_time);
            tvSubscribedBadge = itemView.findViewById(R.id.tv_subscribed_badge);
            tvLatestMoment = itemView.findViewById(R.id.tv_latest_moment);
        }

        void bind(User user, int position) {
            tvNickname.setText(user.getNickname());
            tvActiveTime.setText(TimeUtils.formatActiveTime(user.getLastActiveTime()));

            // 订阅标签
            if (user.isSubscribed()) {
                tvSubscribedBadge.setVisibility(View.VISIBLE);
            } else {
                tvSubscribedBadge.setVisibility(View.GONE);
            }

            // 头像和背景图
            ImageLoader.loadAvatar(itemView.getContext(), user.getAvatar(), ivAvatar);
            ImageLoader.loadBackground(itemView.getContext(), user.getBackgroundImage(), ivBackground);

            // 最近碎碎念预览（如果有的话）
            tvLatestMoment.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onWorldClick(user, position);
            });
        }
    }
}
