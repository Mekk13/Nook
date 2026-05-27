package com.Nook.backend.domain.flashcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CardCreateRequest(
        @NotBlank(message = "Front text is required")
        @Size(max = 500, message = "Front text must be at most 500 characters")
        String front,

        @NotBlank(message = "Back text is required")
        @Size(max = 500, message = "Back text must be at most 500 characters")
        String back
) {}