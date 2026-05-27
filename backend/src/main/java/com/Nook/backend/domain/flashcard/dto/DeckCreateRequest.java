package com.Nook.backend.domain.flashcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeckCreateRequest(
        @NotBlank(message = "Deck name is required")
        @Size(min = 2, max = 60, message = "Deck name must be between 2 and 60 characters")
        String name,

        @Size(max = 100, message = "Topic must be at most 100 characters")
        String topic   // optional
) {}