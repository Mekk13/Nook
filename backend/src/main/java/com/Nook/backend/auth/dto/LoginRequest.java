package com.Nook.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        // Can be either an email address or a username —
        // AuthService figures out which one based on whether it contains "@"
        @NotBlank(message = "Email or username is required")
        String identifier,

        @NotBlank(message = "Password is required")
        String password
) {}