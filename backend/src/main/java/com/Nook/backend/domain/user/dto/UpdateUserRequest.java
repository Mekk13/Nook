package com.Nook.backend.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(min = 2, max = 60, message = "Full name must be 2–60 characters")
        String fullName,

        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, numbers, and underscores")
        @Size(min = 2, max = 30, message = "Username must be 2–30 characters")
        String username,

        @Size(max = 50, message = "Avatar name too long")
        String avatar,

        @Size(max = 200, message = "Description too long")
                String description,

        String currentPassword,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&]).{10,}$", message = "Password too weak")
        String newPassword
) {}