package com.Nook.backend.domain.flashcard.dto;

import jakarta.validation.constraints.Size;

// All fields optional — only update what's provided
public record DeckUpdateRequest(
        @Size(min = 2, max = 60, message = "Deck name must be between 2 and 60 characters")
        String name,

        @Size(max = 100, message = "Topic must be at most 100 characters")
        String topic
) {}