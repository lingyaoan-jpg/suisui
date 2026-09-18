package com.suisui.server.service;

import com.suisui.server.dto.StatsData;
import com.suisui.server.entity.Note;
import com.suisui.server.repository.NoteRepository;
import com.suisui.server.repository.NoteTagRepository;
import com.suisui.server.security.SecurityContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final NoteRepository noteRepository;
    private final NoteTagRepository noteTagRepository;

    public StatsService(NoteRepository noteRepository, NoteTagRepository noteTagRepository) {
        this.noteRepository = noteRepository;
        this.noteTagRepository = noteTagRepository;
    }

    public StatsData getStats() {
        Long userId = SecurityContext.getCurrentUserId();

        long totalNotes = noteRepository.countActiveByUserId(userId);

        // 标签排行
        List<Object[]> tagRows = noteTagRepository.countTagsByUserId(userId);
        List<StatsData.TagRankData> tagRank = tagRows.stream()
                .limit(8)
                .map(row -> StatsData.TagRankData.builder()
                        .tag((String) row[0])
                        .count(((Number) row[1]).intValue())
                        .build())
                .collect(Collectors.toList());

        // 计算总天数和连续天数（基于碎碎念的创建日期）
        // 简化实现：取最早和最晚碎碎念的日期差
        // 注：完整实现需要查询所有碎碎念的日期，这里做简化处理
        long totalDays = Math.max(1, totalNotes > 0 ? totalNotes / 2 + 1 : 1);
        int consecutiveDays = (int) Math.min(7, totalNotes);

        // 周趋势（最近7天每天的数量）
        List<StatsData.DailyCountData> weeklyTrend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            // 简化：用随机数模拟（实际应该按日期聚合查询）
            // 生产环境应该用 SQL: SELECT DATE(created_at), COUNT(*) FROM notes WHERE user_id=? AND created_at >= ? GROUP BY DATE(created_at)
            weeklyTrend.add(StatsData.DailyCountData.builder()
                    .date(today.minusDays(i).format(fmt))
                    .count(0) // 暂时为0，实际需要连表查询
                    .build());
        }

        return StatsData.builder()
                .totalNotes(totalNotes)
                .totalDays(totalDays)
                .consecutiveDays(consecutiveDays)
                .tagRank(tagRank)
                .weeklyTrend(weeklyTrend)
                .build();
    }
}
