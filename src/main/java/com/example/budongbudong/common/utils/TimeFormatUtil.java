package com.example.budongbudong.common.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeFormatUtil {

    private TimeFormatUtil() { }

    public static String formatTime(LocalDateTime time) {

        if (time == null) {
            return "";
        }

        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) {
            return "방금 전";
        }
        if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = duration.toDays();
        return days + "일 전";
    }
}
