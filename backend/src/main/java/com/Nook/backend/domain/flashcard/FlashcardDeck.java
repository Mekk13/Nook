package com.Nook.backend.domain.flashcard;

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
@Table(name = "flashcard_decks")
public class FlashcardDeck {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    @Column(nullable = false)
    private String name;

    private String topic;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}