package com.Nook.backend.domain.session.dto;

public record SessionStatsResponse(
        long totalMinutes,
        long todayMinutes,
        long weekMinutes,
        long monthMinutes,
        int totalSessions,
        int todaySessions,
        int weekSessions,
        int monthSessions
) {}