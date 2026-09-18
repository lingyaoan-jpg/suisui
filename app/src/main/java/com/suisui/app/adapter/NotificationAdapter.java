package com.suisui.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suisui.app.R;
import com.suisui.app.model.Notification;
import com.suisui.app.util.ImageLoader;
import com.suisui.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 通知列表适配器
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification, int position);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(notifications.get(position), position);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvText, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvText = itemView.findViewById(R.id.tv_notification_text);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
        }

        void bind(Notification notification, int position) {
            tvText.setText(notification.getActionDescription());
            tvTime.setText(TimeUtils.formatFriendly(notification.getCreatedAt()));
            ImageLoader.loadAvatar(itemView.getContext(), notification.getFromUserAvatar(), ivAvatar);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onNotificationClick(notification, position);
            });
        }
    }
}
