package com.suisui.app.ui.me;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.suisui.app.R;
import com.suisui.app.api.ApiClient;
import com.suisui.app.api.ApiService;
import com.suisui.app.databinding.ActivityStatisticsBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance(this).getService();
        binding.btnBack.setOnClickListener(v -> finish());
        loadStats();
    }

    private void loadStats() {
        apiService.getStats().enqueue(new Callback<ApiService.ApiResponse<ApiService.StatsData>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<ApiService.StatsData>> call,
                                   Response<ApiService.ApiResponse<ApiService.StatsData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiService.StatsData stats = response.body().data;
                    binding.tvTotalCount.setText(String.valueOf(stats.totalNotes));
                    binding.tvTotalDays.setText(String.valueOf(stats.totalDays));
                    binding.tvConsecutiveDays.setText(String.valueOf(stats.consecutiveDays));

                    if (stats.tagRank != null) buildTagRank(stats.tagRank);
                    if (stats.weeklyTrend != null) buildWeeklyChart(stats.weeklyTrend);
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<ApiService.StatsData>> call, Throwable t) {
                Toast.makeText(StatisticsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildTagRank(List<ApiService.TagRankData> tagRank) {
        if (tagRank.isEmpty()) return;
        binding.rvTagRank.setVisibility(View.GONE);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundResource(R.drawable.bg_card);
        container.setPadding(dp(16), dp(12), dp(16), dp(12));

        int maxCount = tagRank.get(0).count;
        for (int i = 0; i < Math.min(8, tagRank.size()); i++) {
            ApiService.TagRankData entry = tagRank.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));

            TextView tvTag = new TextView(this);
            tvTag.setText(entry.tag);
            tvTag.setTextSize(12);
            tvTag.setTextColor(getResources().getColor(R.color.text_primary));
            tvTag.setLayoutParams(new LinearLayout.LayoutParams(dp(60), ViewGroup.LayoutParams.WRAP_CONTENT));
            row.addView(tvTag);

            View bar = new View(this);
            int barWidth = (int) (dp(150) * ((float) entry.count / maxCount));
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    Math.max(barWidth, dp(4)), dp(16));
            barParams.setMargins(dp(8), 0, dp(8), 0);
            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(0xFFFF8C69);
            bar.setAlpha(0.7f + (0.3f * i / tagRank.size()));
            row.addView(bar);

            TextView tvCount = new TextView(this);
            tvCount.setText(String.valueOf(entry.count));
            tvCount.setTextSize(12);
            tvCount.setTextColor(getResources().getColor(R.color.text_secondary));
            row.addView(tvCount);

            container.addView(row);
        }

        LinearLayout parent = (LinearLayout) binding.layoutChart.getParent();
        int idx = parent.indexOfChild(binding.layoutChart);
        parent.addView(container, idx + 1);
    }

    private void buildWeeklyChart(List<ApiService.DailyCountData> weeklyTrend) {
        if (weeklyTrend.isEmpty()) return;
        binding.layoutChart.removeAllViews();
        binding.layoutChart.setWeightSum(weeklyTrend.size());

        int maxCount = 1;
        for (ApiService.DailyCountData d : weeklyTrend) {
            maxCount = Math.max(maxCount, d.count);
        }

        for (ApiService.DailyCountData day : weeklyTrend) {
            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            column.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            View bar = new View(this);
            int barHeight = maxCount > 0 ? (int) (dp(120) * ((float) day.count / maxCount)) : 0;
            bar.setLayoutParams(new LinearLayout.LayoutParams(dp(20), Math.max(barHeight, dp(4))));
            bar.setBackgroundColor(0xFFFF8C69);
            column.addView(bar);

            TextView tvCount = new TextView(this);
            tvCount.setText(String.valueOf(day.count));
            tvCount.setTextSize(10);
            tvCount.setTextColor(getResources().getColor(R.color.text_primary));
            tvCount.setGravity(Gravity.CENTER);
            tvCount.setPadding(0, dp(4), 0, 0);
            column.addView(tvCount);

            TextView tvDay = new TextView(this);
            try {
                tvDay.setText(day.date.substring(5));
            } catch (Exception e) {
                tvDay.setText(day.date);
            }
            tvDay.setTextSize(10);
            tvDay.setTextColor(getResources().getColor(R.color.text_secondary));
            tvDay.setGravity(Gravity.CENTER);
            tvDay.setPadding(0, dp(2), 0, 0);
            column.addView(tvDay);

            binding.layoutChart.addView(column);
        }
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
