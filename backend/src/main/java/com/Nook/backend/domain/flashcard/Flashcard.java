package com.Nook.backend.domain.flashcard;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flashcards")
public class Flashcard {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(name = "deck_id", nullable = false)
    private String deckId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String front;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String back;

    @Column(name = "creator_id", nullable = false)
    private String creatorId;
}