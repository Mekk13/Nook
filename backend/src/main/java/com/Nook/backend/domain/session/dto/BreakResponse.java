package com.Nook.backend.domain.session.dto;

import com.Nook.backend.domain.session.Break;

public record BreakResponse(
        String id,
        String sessionId,
        String startedAt,
        String endedAt,
        boolean inProgress
) {
    public static BreakResponse from(Break b) {
        return new BreakResponse(
                b.getId(), b.getSessionId(),
                b.getStartedAt().toString(),
                b.getEndedAt() != null ? b.getEndedAt().toString() : null,
                b.getEndedAt() == null
        );
    }
}