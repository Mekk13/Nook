package com.Nook.backend.domain.user;

import com.Nook.backend.domain.user.dto.UpdateUserRequest;
import com.Nook.backend.exception.ConflictException;
import com.Nook.backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;

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
}