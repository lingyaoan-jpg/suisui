package com.suisui.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suisui.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签筛选横向列表适配器
 */
public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private List<String> tags = new ArrayList<>();
    private int selectedPosition = 0; // 0 = "全部"
    private OnTagClickListener listener;

    public interface OnTagClickListener {
        void onTagClick(String tag, int position);
    }

    public TagAdapter(OnTagClickListener listener) {
        this.listener = listener;
    }

    public void setTags(List<String> tags) {
        this.tags = new ArrayList<>();
        this.tags.add("全部"); // 第一项始终是"全部"
        if (tags != null) {
            this.tags.addAll(tags);
        }
        notifyDataSetChanged();
    }

    public String getSelectedTag() {
        if (selectedPosition == 0) return null; // null表示全部
        return tags.get(selectedPosition);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String tag = tags.get(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(tag, isSelected, position);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTagName;

        ViewHolder(View itemView) {
            super(itemView);
            tvTagName = itemView.findViewById(R.id.tv_tag_name);
        }

        void bind(String tag, boolean isSelected, int position) {
            tvTagName.setText(tag);
            if (isSelected) {
                tvTagName.setBackgroundResource(R.drawable.bg_tag_chip_selected);
                tvTagName.setTextColor(0xFFFFFFFF);
            } else {
                tvTagName.setBackgroundResource(R.drawable.bg_tag_chip);
                tvTagName.setTextColor(itemView.getContext().getResources().getColor(R.color.tag_text));
            }

            tvTagName.setOnClickListener(v -> {
                int oldPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onTagClick(selectedPosition == 0 ? null : tag, position);
                }
            });
        }
    }
}
