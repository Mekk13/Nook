package com.Nook.backend.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(name = "full_name")
    private String fullName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private String avatar = "default";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column
    private String description;

    // ── Password reset ────────────────────────────────────────────────────────
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    // ── Magic link (dedicated fields, separate from password reset) ───────────
    @Column(name = "magic_link_token")
    private String magicLinkToken;

    @Column(name = "magic_link_expiry")
    private LocalDateTime magicLinkExpiry;

    // ── Three-factor auth ─────────────────────────────────────────────────────
    /** Whether this user has 3FA enabled (email OTP + TOTP). */
    @Builder.Default
    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    /**
     * Base32-encoded TOTP secret (stored after the user scans the QR code
     * and confirms their first TOTP code). Null until TOTP is set up.
     */
    @Column(name = "totp_secret")
    private String totpSecret;

    /** Short-lived email OTP (6 digits). Cleared after use. */
    @Column(name = "email_otp_code")
    private String emailOtpCode;

    @Column(name = "email_otp_expiry")
    private LocalDateTime emailOtpExpiry;
}