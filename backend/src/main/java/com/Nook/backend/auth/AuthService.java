package com.Nook.backend.auth;

import com.Nook.backend.auth.dto.AuthResponse;
import com.Nook.backend.auth.dto.LoginRequest;
import com.Nook.backend.auth.dto.RegisterRequest;
import com.Nook.backend.domain.user.IUserRepository;
import com.Nook.backend.domain.user.User;
import com.Nook.backend.exception.ConflictException;
import com.Nook.backend.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // BCrypt, from SecurityConfig
    private final JwtUtil jwtUtil;

    public AuthService(IUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        // 1. Check email isn't already taken
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }

        // 2. Check username isn't already taken
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username '" + request.username() + "' is already taken");
        }

        // 3. Build the User
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

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getUsername(), user.getAvatar());
    }

    public AuthResponse login(LoginRequest request) {
        // Decide whether the identifier is an email or a username
        // Simple heuristic: emails contain "@"
        String identifier = request.identifier().toLowerCase();
        boolean isEmail = identifier.contains("@");

        User user = isEmail
                ? userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"))
                : userRepository.findByUsername(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        // We say "Invalid credentials" not "user not found" — never reveal which field was wrong

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getUsername(), user.getAvatar());
    }
}