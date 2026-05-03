package com.Nook.backend.domain.user.dto;

import com.Nook.backend.domain.user.User;

// What we send back to the frontend when returning user data.
// Notice: NO passwordHash field — that never leaves the server.
public record UserResponse(
        String id,
        String fullName,
        String username,
        String email,
        String avatar,
        String createdAt
) {
    // Static factory method — converts a User domain object to a safe response
    // Usage: UserResponse.from(user)
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }
}