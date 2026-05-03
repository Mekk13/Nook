package com.Nook.backend.domain.flashcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// A single card inside a FlashcardDeck.
// Classic front/back format — front has the question or term,
// back has the answer or definition.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flashcard {

    private String id;
    private String deckId;
    private String front;
    private String back;
    private String creatorId;
}