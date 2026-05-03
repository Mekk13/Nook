package com.Nook.backend.domain.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Represents one pause within a study session.
// A session can have many breaks — the user hits pause, then resume, possibly many times.
// At the end of the session the frontend shows:
//   "You studied for 47 minutes, took 2 breaks (5 min + 8 min)"
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Break {

    private String id;
    private String sessionId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}