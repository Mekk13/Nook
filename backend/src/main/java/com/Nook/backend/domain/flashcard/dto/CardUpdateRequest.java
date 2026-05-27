package com.Nook.backend.domain.flashcard.dto;

import jakarta.validation.constraints.Size;

public record CardUpdateRequest(
        @Size(max = 500, message = "Front text must be at most 500 characters")
        String front,

        @Size(max = 500, message = "Back text must be at most 500 characters")
        String back
) {}