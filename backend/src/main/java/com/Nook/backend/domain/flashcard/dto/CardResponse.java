package com.Nook.backend.domain.flashcard.dto;

import com.Nook.backend.domain.flashcard.Flashcard;

public record CardResponse(
        String id,
        String deckId,
        String front,
        String back,
        String creatorId
) {
    public static CardResponse from(Flashcard card) {
        return new CardResponse(
                card.getId(),
                card.getDeckId(),
                card.getFront(),
                card.getBack(),
                card.getCreatorId()
        );
    }
}