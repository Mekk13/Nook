package com.Nook.backend.domain.user;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.user.dto.UpdateUserRequest;
import com.Nook.backend.domain.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me — get my own profile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(UserResponse.from(userService.getMe(userId)));
    }

    // PUT /api/users/me — update my profile
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UpdateUserRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(UserResponse.from(userService.updateMe(userId, request)));
    }

    // DELETE /api/users/me — delete my account
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {
        String userId = SecurityUtils.getCurrentUserId();
        userService.deleteMe(userId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/users — list all users (useful for finding friends later)
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(
                userService.getAllUsers().stream()
                        .map(UserResponse::from)
                        .toList()
        );
    }

    // GET /api/users/{id} — get any user's public profile
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(UserResponse.from(userService.getUserById(id)));
    }
}