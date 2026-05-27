package com.Nook.backend.domain.room.dto;

import jakarta.validation.constraints.*;

public record UpdateRoomRequest(

        @Size(min = 2, max = 60, message = "Room name must be 2–60 characters")
        String name,

        @Size(max = 300, message = "Description too long")
        String description,

        @Min(2) @Max(50)
        Integer maxParticipants,

        Boolean isPrivate
) {}