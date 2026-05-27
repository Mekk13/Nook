package com.Nook.backend.domain.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EndSessionRequest(
        @NotBlank(message = "Session name is required")
        @Size(min = 2, max = 100, message = "Session name must be 2–100 characters")
        String name,

        @NotBlank(message = "Subject is required")
        @Size(min = 2, max = 100, message = "Subject must be 2–100 characters")
        String subject
) {}