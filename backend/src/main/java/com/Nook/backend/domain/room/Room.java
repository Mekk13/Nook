package com.Nook.backend.domain.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    private String id;

    private String name;
    private String creatorId;
    private String roomCode;
    private String description;
    @Builder.Default
    private int maxParticipants = 10;
    // Private rooms can only be joined via room code.
    @Builder.Default
    private boolean isPrivate = false;
    private LocalDateTime createdAt;
}