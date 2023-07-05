package com.paddi.common.utils;

import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-bilibili-server
 * @CreatedTime: 2023年06月29日 01:03:52
 */
public class TimeUtils {

    /**
     * 将表示分钟和秒钟的字符串转化为秒数
     *
     * @param timeStr 分钟和秒钟的字符串，格式为 MM:SS
     * @return 秒数
     * @throws DateTimeParseException 如果字符串格式不正确，将抛出异常
     */
    public static int parseTimeToSeconds(String timeStr) throws DateTimeParseException {
        String[] parts = timeStr.split(":");
        if (parts.length != 2) {
            throw new DateTimeParseException("Invalid time string: " + timeStr, timeStr, 0);
        }
        String durationStr = "PT" + parts[0] + "M" + parts[1] + "S";
        Duration duration = Duration.parse(durationStr);
        return (int) duration.getSeconds();
    }

    /**
     * 将秒数转化为表示分钟和秒钟的字符串
     *
     * @param seconds 秒数
     * @return 分钟和秒钟的字符串，格式为 MM:SS
     */
    public static String transformToTime(int seconds) {
        Duration duration = Duration.ofSeconds(seconds);
        long minutes = duration.toMinutes();
        long secondsPart = duration.getSeconds() % 60;
        return String.format("%02d:%02d", minutes, secondsPart);
    }

    public static void main(String[] args) {
        System.out.println(parseTimeToSeconds("05:20"));
        System.out.println(transformToTime(320));
    }
}