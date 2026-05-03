package com.Nook.backend.domain.flashcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// A deck of flashcards that lives inside a room.
// Any member of the room can see and study the deck.
// Only OWNER or MODERATOR can delete the deck.
// Any member can add cards to it.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardDeck {

    private String id;
    private String roomId;
    private String creatorId;
    private String name;
    private String topic;
    private LocalDateTime createdAt;
}