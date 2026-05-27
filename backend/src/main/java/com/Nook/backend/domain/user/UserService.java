package com.Nook.backend.domain.user;

import com.Nook.backend.auth.EmailService;
import com.Nook.backend.domain.user.dto.UpdateUserRequest;
import com.Nook.backend.exception.ConflictException;
import com.Nook.backend.exception.NotFoundException;
import com.Nook.backend.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;
    private final EmailService emailService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // Inside UserService.java
    public void initiatePasswordReset(String email) {
        // 1. Find user (must exist in DB for this to work!)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.Nook.backend.exception.NotFoundException("User not found"));

        // 2. Create Token
        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));

        // 3. Save to DB
        userRepository.update(user);

        // 4. Send the real email via Mailtrap
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    public User getMe(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User updateMe(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.username() != null) {
            String newUsername = request.username().toLowerCase();
            if (!newUsername.equals(user.getUsername()) &&
                    userRepository.existsByUsername(newUsername)) {
                throw new ConflictException("Username already taken");
            }
            user.setUsername(newUsername);
        }
        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.avatar() != null) user.setAvatar(request.avatar());

        if (request.description() != null) user.setDescription(request.description());

        if (request.newPassword() != null) {
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new UnauthorizedException("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        userRepository.update(user);
        return user;
    }

    public void deleteMe(String userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        userRepository.delete(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public void completePasswordReset(String token, String newPassword) {
        // 1. Find user by token
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid token"));

        // 2. Check if expired - Use ResponseStatusException to avoid the "Unhandled Exception" error
        if (user.getTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Token expired");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        user.setResetToken(null);
        user.setTokenExpiry(null);

        userRepository.update(user);
    }
}