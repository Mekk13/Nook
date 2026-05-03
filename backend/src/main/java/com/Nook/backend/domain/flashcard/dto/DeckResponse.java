package com.Nook.backend.domain.flashcard.dto;

import com.Nook.backend.domain.flashcard.FlashcardDeck;

public record DeckResponse(
        String id,
        String roomId,
        String creatorId,
        String name,
        String topic,
        String createdAt,
        int cardCount   // convenience field — how many cards are in this deck
) {
    public static DeckResponse from(FlashcardDeck deck, int cardCount) {
        return new DeckResponse(
                deck.getId(),
                deck.getRoomId(),
                deck.getCreatorId(),
                deck.getName(),
                deck.getTopic(),
                deck.getCreatedAt() != null ? deck.getCreatedAt().toString() : null,
                cardCount
        );
    }
}