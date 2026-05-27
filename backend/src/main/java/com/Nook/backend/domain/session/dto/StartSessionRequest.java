package com.Nook.backend.domain.session.dto;

import jakarta.validation.constraints.NotBlank;

public record StartSessionRequest(
        @NotBlank(message = "Room ID is required")
        String roomId
) {}