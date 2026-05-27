package com.Nook.backend.auth;

import com.Nook.backend.auth.dto.AuthResponse;
import com.Nook.backend.domain.user.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.Nook.backend.domain.user.User;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final IUserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestClient restClient;

    @Value("${oauth.google.client-id}")
    private String googleClientId;
    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;
    @Value("${oauth.github.client-id}")
    private String githubClientId;
    @Value("${oauth.github.client-secret}")
    private String githubClientSecret;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthResponse handleGoogleCallback(String code) {
        // Step 1: exchange code for access token
        Map<String, String> tokenRequest = Map.of(
                "code", code,
                "client_id", googleClientId,
                "client_secret", googleClientSecret,
                "redirect_uri", frontendUrl + "/oauth/callback/google",
                "grant_type", "authorization_code"
        );

        Map tokenResponse = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(tokenRequest)
                .retrieve()
                .body(Map.class);

        String accessToken = (String) tokenResponse.get("access_token");

        // Step 2: get user info from Google
        Map userInfo = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        return findOrCreateOAuthUser(email, name, "google");
    }

    public AuthResponse handleGithubCallback(String code) {
        // Step 1: exchange code for access token
        Map<String, String> tokenRequest = Map.of(
                "code", code,
                "client_id", githubClientId,
                "client_secret", githubClientSecret
        );

        Map tokenResponse = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(tokenRequest)
                .retrieve()
                .body(Map.class);

        String accessToken = (String) tokenResponse.get("access_token");

        // Step 2: get user info from GitHub
        Map userInfo = restClient.get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        // GitHub may not expose email publicly — fall back to login
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.getOrDefault("name", userInfo.get("login"));

        // ADD THIS:
        if (email == null) {
            email = userInfo.get("login") + "@github.local";
        }
        return findOrCreateOAuthUser(email, name, "github");
    }

    private AuthResponse findOrCreateOAuthUser(String email, String name, String provider) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> {
                    // New user via OAuth — create them without a password
                    User newUser = User.builder()
                            .id(UUID.randomUUID().toString())
                            .email(email.toLowerCase())
                            .fullName(name)
                            .username(generateUsername(email))
                            .passwordHash("OAUTH_" + provider) // not usable for login
                            .avatar("default")
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });

        String jwt = jwtUtil.generateToken(user.getId(), user.getRole().name());
        return new AuthResponse(jwt, user.getId(), user.getFullName(),
                user.getUsername(), user.getAvatar(), user.getRole().name());
    }

    private String generateUsername(String email) {
        // e.g. "john.doe@gmail.com" → "john.doe" + random suffix if taken
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}