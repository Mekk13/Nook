package com.Nook.backend.domain.user;

import com.Nook.backend.domain.user.dto.UpdateUserRequest;
import com.Nook.backend.exception.ConflictException;
import com.Nook.backend.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock IUserRepository userRepository;
    @InjectMocks UserService userService;

    private User user() {
        return User.builder()
                .id("u1")
                .username("john")
                .fullName("John Doe")
                .email("john@test.com")
                .avatar("default")
                .build();
    }

    @Test
    void getMe_returnsUser() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user()));

        User result = userService.getMe("u1");

        assertThat(result.getUsername()).isEqualTo("john");
    }

    @Test
    void getMe_notFound_throws() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe("u1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateMe_updatesAllFields() {
        User user = User.builder()
                .id("u1")
                .username("john")
                .fullName("John")
                .avatar("default")
                .build();

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        // 1. Stub the exact lowercase string the service will produce
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // 2. Fix the request: username is "newuser", fullName is "New Name"
        UpdateUserRequest req = new UpdateUserRequest("New Name", "newuser", "avatar.png");

        User result = userService.updateMe("u1", req);

        // 3. Assertions
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getFullName()).isEqualTo("New Name");
        assertThat(result.getAvatar()).isEqualTo("avatar.png");

        verify(userRepository).update(user);
    }

    @Test
    void updateMe_usernameTaken_throwsConflict() {
        User existing = User.builder()
                .id("u1")
                .username("current") // Give it a specific current username
                .build();

        when(userRepository.findById("u1")).thenReturn(Optional.of(existing));

        // The service will lowercase this to "taken"
        UpdateUserRequest req = new UpdateUserRequest(null, "TAKEN", null);

        // FIX: Stub the lowercased version "taken"
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateMe("u1", req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteMe_deletesUser() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user()));

        userService.deleteMe("u1");

        verify(userRepository).delete("u1");
    }

    @Test
    void deleteMe_notFound_throws() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteMe("u1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user()));

        assertThat(userService.getAllUsers()).hasSize(1);
    }

    @Test
    void getUserById_returnsUser() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user()));

        assertThat(userService.getUserById("u1")).isNotNull();
    }

    @Test
    void getUserByEmail_returnsUser() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user()));

        assertThat(userService.getUserByEmail("john@test.com")).isNotNull();
    }

    @Test
    void getUserByUsername_returnsUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user()));

        assertThat(userService.getUserByUsername("john")).isNotNull();
    }
}