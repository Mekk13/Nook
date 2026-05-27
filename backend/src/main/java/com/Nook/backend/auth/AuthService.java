package com.Nook.backend.auth;

import com.Nook.backend.auth.dto.*;
import com.Nook.backend.domain.user.IUserRepository;
import com.Nook.backend.domain.user.User;
import com.Nook.backend.exception.ConflictException;
import com.Nook.backend.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final TotpService totpService;
    private final MfaSessionStore mfaSessionStore;

    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&]).{10,}$";

    // ── Registration ──────────────────────────────────────────────────────────

    public AuthResponse register(RegisterRequest request) {
        validatePassword(request.password());

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username '" + request.username() + "' is already taken");
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .fullName(request.fullName())
                .username(request.username().toLowerCase())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .avatar("default")
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getRole().name());
        return buildAuthResponse(user, token);
    }

    // ── Login — Step 1: password ──────────────────────────────────────────────

    /**
     * Returns either:
     *   - {@link AuthResponse}       → MFA is OFF, login complete
     *   - {@link MfaRequiredResponse}→ MFA is ON,  proceed to step 2
     *
     * The return type is Object so the controller can write the correct JSON.
     */
    public Object login(LoginRequest request) {
        String identifier = request.identifier().toLowerCase();
        boolean isEmail = identifier.contains("@");

        User user = isEmail
                ? userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"))
                : userRepository.findByUsername(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // No MFA — issue JWT right away
        if (!user.isMfaEnabled()) {
            String token = jwtUtil.generateToken(user.getId(), user.getRole().name());
            return buildAuthResponse(user, token);
        }

        // MFA enabled — send email OTP and return a session token
        String otp = generateOtp();
        user.setEmailOtpCode(passwordEncoder.encode(otp)); // store hashed
        user.setEmailOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);

        String mfaSessionToken = mfaSessionStore.create(user.getId());
        return new MfaRequiredResponse("EMAIL_OTP", mfaSessionToken);
    }

    // ── Login — Step 2: email OTP ─────────────────────────────────────────────

    /**
     * Validates the email OTP. If OK, returns:
     *   - {@link MfaRequiredResponse} with nextStep="TOTP" → user must enter TOTP code
     */
    public MfaRequiredResponse verifyEmailOtp(VerifyEmailOtpRequest request) {
        String userId = mfaSessionStore.getUserId(request.mfaSessionToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getEmailOtpExpiry() == null ||
                LocalDateTime.now().isAfter(user.getEmailOtpExpiry())) {
            throw new UnauthorizedException("OTP has expired");
        }

        if (!passwordEncoder.matches(request.code(), user.getEmailOtpCode())) {
            throw new UnauthorizedException("Incorrect OTP code");
        }

        // Clear the OTP — single use
        user.setEmailOtpCode(null);
        user.setEmailOtpExpiry(null);
        userRepository.save(user);

        // Same mfaSessionToken is reused for the TOTP step
        return new MfaRequiredResponse("TOTP", request.mfaSessionToken());
    }

    // ── Login — Step 3: TOTP ──────────────────────────────────────────────────

    /**
     * Validates the TOTP code. If OK, issues the real JWT and clears the MFA session.
     */
    public AuthResponse verifyTotp(VerifyTotpRequest request) {
        String userId = mfaSessionStore.getUserId(request.mfaSessionToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getTotpSecret() == null) {
            throw new UnauthorizedException("TOTP not configured");
        }

        if (!totpService.verifyCode(user.getTotpSecret(), request.code())) {
            throw new UnauthorizedException("Incorrect authenticator code");
        }

        mfaSessionStore.remove(request.mfaSessionToken());

        String jwt = jwtUtil.generateToken(user.getId(), user.getRole().name());
        return buildAuthResponse(user, jwt);
    }

    // ── TOTP setup (called from settings) ────────────────────────────────────

    /**
     * Generates a fresh TOTP secret and returns a QR code for the user to scan.
     * The secret is NOT saved yet — it's saved only after the user confirms with
     * {@link #confirmTotpSetup}.
     */
    public TotpSetupResponse initiateTotpSetup(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String secret = totpService.generateSecret();
        String qrCodeDataUri = totpService.generateQrCodeDataUri(user.getEmail(), secret);

        return new TotpSetupResponse(qrCodeDataUri, secret);
    }

    /**
     * Verifies the user's first TOTP code (confirming they scanned correctly),
     * then persists the secret and enables MFA.
     */
    public void confirmTotpSetup(String userId, ConfirmTotpSetupRequest request) {
        System.out.println("secret: " + request.secret());
        System.out.println("code: " + request.code());
        if (!totpService.verifyCode(request.secret(), request.code())) {
            throw new UnauthorizedException("Code doesn't match — please try scanning again");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        user.setTotpSecret(request.secret());
        user.setMfaEnabled(true);
        userRepository.save(user);
    }

    /** Disables MFA and clears the TOTP secret. */
    public void disableMfa(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        user.setMfaEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
    }

    // ── Magic link (fixed — uses dedicated fields, not resetToken) ────────────

    public void sendMagicLink(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("No account found with that email"));

        String token = UUID.randomUUID().toString();
        user.setMagicLinkToken(token);
        user.setMagicLinkExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendMagicLinkEmail(email, token);
    }

    public AuthResponse verifyMagicLink(String token) {
        User user = userRepository.findByMagicLinkToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired link"));

        if (user.getMagicLinkExpiry() == null ||
                user.getMagicLinkExpiry().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Magic link has expired");
        }

        // One-time use
        user.setMagicLinkToken(null);
        user.setMagicLinkExpiry(null);
        userRepository.save(user);

        String jwt = jwtUtil.generateToken(user.getId(), user.getRole().name());
        return buildAuthResponse(user, jwt);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private void validatePassword(String password) {
        if (password == null || !password.matches(PASSWORD_PATTERN)) {
            throw new IllegalArgumentException(
                    "Password must be at least 10 characters and include uppercase, " +
                            "lowercase, a digit, and a special character.");
        }
    }

    /** Generates a cryptographically random 6-digit OTP. */
    private String generateOtp() {
        SecureRandom rng = new SecureRandom();
        int code = 100_000 + rng.nextInt(900_000); // 100000–999999
        return String.valueOf(code);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getAvatar(),
                user.getRole().name()
        );
    }
}