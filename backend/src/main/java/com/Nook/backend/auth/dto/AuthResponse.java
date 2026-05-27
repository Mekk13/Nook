package com.Nook.backend.auth.dto;

public record AuthResponse(
        String token,
        String userId,
        String displayName,
        String username,
        String avatar,
        String role
) {}