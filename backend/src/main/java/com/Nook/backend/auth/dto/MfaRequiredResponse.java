// ── MfaRequiredResponse.java ──────────────────────────────────────────────────
// Returned after step 1 (password OK) when MFA is enabled.
// Frontend uses this to show the OTP input screen.
package com.Nook.backend.auth.dto;

public record MfaRequiredResponse(
        /** Always "EMAIL_OTP" for step 2. Frontend checks this to know what to show. */
        String nextStep,
        /** Short-lived session token that ties the three steps together. */
        String mfaSessionToken
) {}