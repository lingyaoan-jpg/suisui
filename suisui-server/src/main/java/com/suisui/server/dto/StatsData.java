package com.suisui.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsData {

    private long totalNotes;
    private long totalDays;
    private int consecutiveDays;
    private List<TagRankData> tagRank;
    private List<DailyCountData> weeklyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagRankData {
        private String tag;
        private int count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCountData {
        private String date;
        private int count;
    }
}
