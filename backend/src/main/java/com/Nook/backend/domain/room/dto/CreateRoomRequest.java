package com.Nook.backend.domain.room.dto;

import jakarta.validation.constraints.*;

public record CreateRoomRequest(

        @NotBlank(message = "Room name is required")
        @Size(min = 2, max = 60, message = "Room name must be 2–60 characters")
        String name,

        @Size(max = 300, message = "Description too long")
        String description,

        @Min(value = 2, message = "Room must allow at least 2 participants")
        @Max(value = 50, message = "Room cannot exceed 50 participants")
        Integer maxParticipants,

        Boolean isPrivate
) {}