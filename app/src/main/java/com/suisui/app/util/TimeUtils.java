package com.suisui.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 时间格式化工具
 */
public class TimeUtils {

    private static final SimpleDateFormat TODAY_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final SimpleDateFormat YESTERDAY_FORMAT = new SimpleDateFormat("昨天 HH:mm", Locale.CHINA);
    private static final SimpleDateFormat THIS_YEAR_FORMAT = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);
    private static final SimpleDateFormat FULL_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);

    /**
     * 格式化时间为友好显示
     */
    public static String formatFriendly(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // 刚刚（1分钟内）
        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "刚刚";
        }
        // x分钟前（1小时内）
        if (diff < TimeUnit.HOURS.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toMinutes(diff) + "分钟前";
        }
        // x小时前（24小时内）
        if (diff < TimeUnit.DAYS.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toHours(diff) + "小时前";
        }

        // 获取日历信息
        Calendar nowCal = Calendar.getInstance();
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTimeInMillis(timestamp);

        Date date = new Date(timestamp);
        // 昨天
        if (nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) - targetCal.get(Calendar.DAY_OF_YEAR) == 1) {
            return YESTERDAY_FORMAT.format(date);
        }
        // 今年
        if (nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)) {
            return THIS_YEAR_FORMAT.format(date);
        }
        return FULL_FORMAT.format(date);
    }

    /**
     * 解析服务器返回的日期字符串 "yyyy-MM-dd HH:mm:ss" 为时间戳
     */
    public static long parseServerDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Date date = sdf.parse(dateStr);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 格式化时间为活跃显示
     */
    public static String formatActiveTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "刚刚活跃";
        }
        if (diff < TimeUnit.HOURS.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toMinutes(diff) + "分钟前活跃";
        }
        if (diff < TimeUnit.DAYS.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toHours(diff) + "小时前活跃";
        }
        if (diff < TimeUnit.DAYS.toMillis(7)) {
            return TimeUnit.MILLISECONDS.toDays(diff) + "天前活跃";
        }
        return "最近活跃";
    }
}
