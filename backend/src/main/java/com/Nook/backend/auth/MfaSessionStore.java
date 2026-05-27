package com.Nook.backend.auth;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridges the three steps of the MFA login flow without issuing a real JWT yet.
 *
 * Step 1 — password OK  → store(userId) → returns mfaSessionToken
 * Step 2 — email OTP OK → validateAndGet(token) → still returns same token (TOTP step next)
 * Step 3 — TOTP OK      → validateAndGet(token) → returns userId → issue real JWT → remove(token)
 *
 * Tokens expire after 10 minutes. A background sweep removes stale entries.
 */
@Component
public class MfaSessionStore {

    private record Entry(String userId, Instant expiresAt) {}

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private static final long TTL_SECONDS = 600; // 10 minutes

    /** Creates a new MFA session token for a user and stores it. */
    public String create(String userId) {
        String token = UUID.randomUUID().toString();
        store.put(token, new Entry(userId, Instant.now().plusSeconds(TTL_SECONDS)));
        return token;
    }

    /**
     * Returns the userId for a valid token, or throws if missing/expired.
     * Does NOT remove the token — call {@link #remove} after the final step.
     */
    public String getUserId(String token) {
        Entry entry = store.get(token);
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
            store.remove(token); // clean up expired entry
            throw new com.Nook.backend.exception.UnauthorizedException("MFA session expired or invalid");
        }
        return entry.userId();
    }

    /** Removes the token after the final MFA step succeeds. */
    public void remove(String token) {
        store.remove(token);
    }

    /** Optional: call from a scheduled task to clean stale entries. */
    public void sweep() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }
}