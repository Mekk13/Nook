package com.Nook.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyTotpRequest(
        @NotBlank String mfaSessionToken,
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "TOTP code must be exactly 6 digits")
        String code
) {}