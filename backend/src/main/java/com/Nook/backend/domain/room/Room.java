package com.Nook.backend.domain.room;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    @Column(name = "room_code", nullable = false, unique = true, length = 6)
    private String roomCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_participants", nullable = false)
    @Builder.Default
    private int maxParticipants = 10;

    @Column(name = "is_private", nullable = false)
    @Builder.Default
    private boolean isPrivate = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}