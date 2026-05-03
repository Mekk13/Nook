package com.Nook.backend.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;
    private String fullName;
    private String username;
    private String email;
    private String passwordHash;
    @Builder.Default
    private String avatar = "default";

    // When the account was created — set automatically in AuthService
    private LocalDateTime createdAt;
}