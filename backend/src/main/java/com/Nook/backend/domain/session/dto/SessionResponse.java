package com.Nook.backend.domain.session.dto;

import com.Nook.backend.domain.session.StudySession;
import java.time.temporal.ChronoUnit; // Added for math
import java.util.List;

public record SessionResponse(
        String id,
        String userId,
        String roomId,
        String name,
        String subject,
        String startedAt,
        String endedAt,
        boolean inProgress,
        double durationHours, // Added this field
        List<BreakResponse> breaks
) {
    public static SessionResponse from(StudySession s) {
        // Calculate duration on the fly
        double duration = 0;
        if (s.getStartedAt() != null && s.getEndedAt() != null) {
            long minutes = ChronoUnit.MINUTES.between(s.getStartedAt(), s.getEndedAt());

            // Subtract breaks if they exist
            long breakMinutes = s.getBreaks().stream()
                    .filter(b -> b.getEndedAt() != null)
                    .mapToLong(b -> ChronoUnit.MINUTES.between(b.getStartedAt(), b.getEndedAt()))
                    .sum();

            duration = (minutes - breakMinutes) / 60.0;
        }

        return new SessionResponse(
                s.getId(), s.getUserId(), s.getRoomId(),
                s.getName(), s.getSubject(),
                s.getStartedAt().toString(),
                s.getEndedAt() != null ? s.getEndedAt().toString() : null,
                s.getEndedAt() == null,
                Math.max(0, duration), // Pass the calculated value
                s.getBreaks().stream().map(BreakResponse::from).toList()
        );
    }
}