package com.Nook.backend.domain.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Represents one complete study session.
// Flow:
//   1. User hits "Start" → session created with startedAt set, endedAt null
//   2. User hits "Pause" → a Break is created with startedAt set
//   3. User hits "Resume" → that Break gets endedAt set
//   4. User hits "Finish" → they name the session + pick a subject,
//      endedAt is set on the session, it's now complete and saved to history
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySession {

    private String id;
    private String userId;
    private String roomId;
    private String name;
    private String subject;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    // All the pauses taken during this session
    // @Builder.Default is needed to make Lombok's builder initialise this
    // as an empty list instead of null
    @Builder.Default
    private List<Break> breaks = new ArrayList<>();
}