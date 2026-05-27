package com.Nook.backend.auth;

import com.Nook.backend.auth.dto.*;
import com.Nook.backend.domain.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final OAuthService oAuthService;

    // ── Registration / login ──────────────────────────────────────────────────

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Step 1 of login.
     * Returns either AuthResponse (MFA off) or MfaRequiredResponse (MFA on).
     * The frontend inspects the JSON: if it contains "mfaSessionToken" → show OTP screen.
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        Object result = authService.login(request);
        return ResponseEntity.ok(result);
    }

    // ── MFA login flow ────────────────────────────────────────────────────────

    /** Step 2: validate the 6-digit email OTP. Returns nextStep=TOTP. */
    @PostMapping("/mfa/verify-email-otp")
    public MfaRequiredResponse verifyEmailOtp(@Valid @RequestBody VerifyEmailOtpRequest request) {
        return authService.verifyEmailOtp(request);
    }

    /** Step 3: validate the TOTP code. Returns full AuthResponse with JWT. */
    @PostMapping("/mfa/verify-totp")
    public AuthResponse verifyTotp(@Valid @RequestBody VerifyTotpRequest request) {
        return authService.verifyTotp(request);
    }

    // ── TOTP setup (settings page, requires authentication) ───────────────────

    /**
     * Generates a fresh TOTP secret + QR code for the current user.
     * Requires a valid JWT (user must be logged in).
     */
    @PostMapping("/mfa/setup")
    public TotpSetupResponse initiateTotpSetup() {
        String userId = SecurityUtils.getCurrentUserId();
        return authService.initiateTotpSetup(userId);
    }

    /**
     * User scanned the QR code and submits their first 6-digit code to confirm.
     * Saves the secret and enables MFA if the code is correct.
     */
    @PostMapping("/mfa/setup/confirm")
    public ResponseEntity<Void> confirmTotpSetup(@Valid @RequestBody ConfirmTotpSetupRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        authService.confirmTotpSetup(userId, request);
        return ResponseEntity.ok().build();
    }

    /** Disables MFA and clears the TOTP secret for the current user. */
    @DeleteMapping("/mfa/setup")
    public ResponseEntity<Void> disableMfa() {
        String userId = SecurityUtils.getCurrentUserId();
        authService.disableMfa(userId);
        return ResponseEntity.ok().build();
    }

    // ── Password reset ────────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.initiatePasswordReset(request.email());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.completePasswordReset(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    // ── Magic link ────────────────────────────────────────────────────────────

    @PostMapping("/magic-link")
    public ResponseEntity<Void> requestMagicLink(@RequestBody MagicLinkRequest request) {
        authService.sendMagicLink(request.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/magic-link/verify")
    public AuthResponse verifyMagicLink(@RequestParam String token) {
        return authService.verifyMagicLink(token);
    }

    // ── OAuth ─────────────────────────────────────────────────────────────────

    @GetMapping("/oauth/google/callback")
    public AuthResponse googleCallback(@RequestParam String code) {
        return oAuthService.handleGoogleCallback(code);
    }

    @GetMapping("/oauth/github/callback")
    public AuthResponse githubCallback(@RequestParam String code) {
        return oAuthService.handleGithubCallback(code);
    }
}