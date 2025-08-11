package com.ml.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于Java 8+ LocalDate的日期工具类
 * 提供常用日期操作功能，线程安全
 */
public class DateUtils {

    // 默认日期格式
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 私有构造方法，防止实例化
    private DateUtils() {}

    /**
     * 获取今天的日期字符串（默认格式）
     */
    public static String getTodayStr() {
        return formatDate(LocalDate.now());
    }

    /**
     * 获取今天的日期字符串（自定义格式）
     * @param pattern 日期格式，如 "yyyy/MM/dd"
     */
    public static String getTodayStr(String pattern) {
        return formatDate(LocalDate.now(), pattern);
    }

    /**
     * 获取本月第一天的日期字符串（默认格式）
     */
    public static String getFirstDayOfMonthStr() {
        return formatDate(getFirstDayOfMonth());
    }

    /**
     * 获取本月第一天的LocalDate对象
     */
    public static LocalDate getFirstDayOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取本月最后一天的LocalDate对象
     */
    public static LocalDate getLastDayOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 格式化日期为字符串（默认格式）
     */
    public static String formatDate(LocalDate date) {
        return date.format(DEFAULT_FORMATTER);
    }

    /**
     * 格式化日期为字符串（自定义格式）
     * @param date 日期对象
     * @param pattern 日期格式，如 "yyyy年MM月dd日"
     */
    public static String formatDate(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取N天前的日期字符串（默认格式）
     * @param days 天数（正数表示过去）
     */
    public static String getDaysBeforeStr(long days) {
        return formatDate(LocalDate.now().minusDays(days));
    }

    /**
     * 获取N天后的日期字符串（默认格式）
     * @param days 天数（正数表示未来）
     */
    public static String getDaysAfterStr(long days) {
        return formatDate(LocalDate.now().plusDays(days));
    }

    /**
     * 获取本周第一天的日期（周一）
     */
    public static LocalDate getFirstDayOfWeek() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    }

    /**
     * 获取本周最后一天的日期（周日）
     */
    public static LocalDate getLastDayOfWeek() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
    }

    /**
     * 获取两个日期之间的所有日期字符串（默认格式）
     * @param start 开始日期字符串（格式：yyyy-MM-dd）
     * @param end 结束日期字符串（格式：yyyy-MM-dd）
     * @return 日期字符串列表
     */
    public static List<String> getDateStringsBetween(String start, String end) {
        return getDateStringsBetween(start, end, DEFAULT_FORMATTER);
    }

    /**
     * 获取两个日期之间的所有日期字符串（自定义格式）
     * @param start 开始日期字符串
     * @param end 结束日期字符串
     * @param pattern 日期格式模式
     * @return 日期字符串列表
     */
    public static List<String> getDateStringsBetween(String start, String end, String pattern) {
        return getDateStringsBetween(start, end, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取两个日期之间的所有日期字符串（使用指定格式器）
     * @param start 开始日期字符串
     * @param end 结束日期字符串
     * @param formatter 日期格式器
     * @return 日期字符串列表
     */
    public static List<String> getDateStringsBetween(String start, String end, DateTimeFormatter formatter) {
        LocalDate startDate = parseDate(start);
        LocalDate endDate = parseDate(end);
        return getDatesBetween(startDate, endDate).stream()
                .map(DateUtils::formatDate)
                .collect(Collectors.toList());
    }

    /**
     * 获取两个日期之间的所有日期字符串（使用LocalDate参数）
     * @param start 开始日期
     * @param end 结束日期
     * @return 日期字符串列表（默认格式）
     */
    public static List<String> getDateStringsBetween(LocalDate start, LocalDate end) {
        return getDatesBetween(start, end).stream()
                .map(DateUtils::formatDate)
                .collect(Collectors.toList());
    }

    /**
     * 获取日期范围内的所有日期
     */
    public static List<LocalDate> getDatesBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("开始日期和结束日期不能为空");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * 判断日期是否是周末
     */
    public static boolean isWeekend(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
    }

    /**
     * 判断日期是否是今天
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }

    /**
     * 将字符串解析为LocalDate（默认格式）
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DEFAULT_FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式不正确，应为 yyyyMMdd: " + dateStr, e);
        }
    }

    /**
     * 将字符串解析为LocalDate（自定义格式）
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取昨天的日期字符串（默认格式）
     */
    public static String getYesterdayStr() {
        return formatDate(LocalDate.now().minusDays(1));
    }

    /**
     * 获取上月字符串 (yyyyMM)
     */
    public static String getLastMonthStr(String format) {
        return LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 获取某日期是当月第几天 (1-31)
     * @param dateStr
     */
    public static int getDayOfMonth(String dateStr) {
        return LocalDate.parse(dateStr).getDayOfMonth();
    }
}