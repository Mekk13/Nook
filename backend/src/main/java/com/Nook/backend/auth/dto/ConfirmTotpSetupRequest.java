package com.Nook.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Sent by the frontend after the user scans the QR code and enters their first code. */
public record ConfirmTotpSetupRequest(
        /** The raw Base32 secret that was shown alongside the QR code. */
        @NotBlank String secret,
        /** The 6-digit code from the authenticator app to confirm it's working. */
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "TOTP code must be 6 digits")
        String code
) {}