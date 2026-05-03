package com.Nook.backend.domain.user;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.user.dto.UpdateUserRequest;
import com.Nook.backend.domain.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock UserService userService;
    @InjectMocks UserController userController;

    private User user() {
        return User.builder()
                .id("u1")
                .username("john")
                .fullName("John Doe")
                .email("john@test.com")
                .build();
    }

    @Test
    void getMe_returns200() {
        when(userService.getMe("u1")).thenReturn(user());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("u1");

            ResponseEntity<UserResponse> res = userController.getMe();

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void updateMe_returns200() {
        when(userService.updateMe(eq("u1"), any())).thenReturn(user());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("u1");

            ResponseEntity<UserResponse> res =
                    userController.updateMe(new UpdateUserRequest("john", "John", "avatar"));

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void deleteMe_returns204() {
        doNothing().when(userService).deleteMe("u1");

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("u1");

            ResponseEntity<Void> res = userController.deleteMe();

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Test
    void getAllUsers_returns200() {
        when(userService.getAllUsers()).thenReturn(List.of(user()));

        ResponseEntity<List<UserResponse>> res = userController.getAllUsers();

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getUserById_returns200() {
        when(userService.getUserById("u1")).thenReturn(user());

        ResponseEntity<UserResponse> res = userController.getUserById("u1");

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}