package com.Nook.backend.domain.membership;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.membership.dto.MembershipResponse;
import com.Nook.backend.domain.membership.dto.UpdateRoleRequest;
import com.Nook.backend.domain.membership.dto.UpdateStatusRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipControllerTest {

    @Mock
    MembershipService membershipService;

    @InjectMocks
    MembershipController membershipController;

    private MembershipResponse sampleMembership() {
        return new MembershipResponse(
                "m1",
                "user-1",
                "room-1",
                MemberRole.MEMBER,
                MemberStatus.IDLE, // replace if your enum differs
                LocalDateTime.now().toString()
        );
    }

    @Test
    void getMyMembership_returns200() {
        when(membershipService.getMembership("user-1","room-1"))
                .thenReturn(sampleMembership());

        try(MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<MembershipResponse> response =
                    membershipController.getMyMembership("room-1");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void joinRoom_returns201() {
        when(membershipService.joinRoom("user-1","room-1"))
                .thenReturn(sampleMembership());

        try(MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<MembershipResponse> response =
                    membershipController.joinRoom("room-1");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Test
    void leaveRoom_returns204() {
        doNothing().when(membershipService)
                .leaveRoom("user-1","room-1");

        try(MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<Void> response =
                    membershipController.leaveRoom("room-1");

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Test
    void updateStatus_returns200() {
        MembershipResponse updated = new MembershipResponse(
                "m1",
                "user-1",
                "room-1",
                MemberRole.MEMBER,
                MemberStatus.IDLE, // replace if needed
                LocalDateTime.now().toString()
        );

        when(membershipService.updateStatus(
                eq("user-1"),
                eq("room-1"),
                any(UpdateStatusRequest.class)))
                .thenReturn(updated);

        try(MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("user-1");

            ResponseEntity<MembershipResponse> response =
                    membershipController.updateStatus(
                            "room-1",
                            new UpdateStatusRequest(MemberStatus.IDLE)
                    );

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void updateRole_returns200() {
        MembershipResponse promoted = new MembershipResponse(
                "m2",
                "user-2",
                "room-1",
                MemberRole.MODERATOR,
                MemberStatus.IDLE,
                LocalDateTime.now().toString()
        );

        when(membershipService.updateRole(
                eq("owner"),
                eq("room-1"),
                eq("user-2"),
                any(UpdateRoleRequest.class)))
                .thenReturn(promoted);

        try(MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("owner");

            ResponseEntity<MembershipResponse> response =
                    membershipController.updateRole(
                            "room-1",
                            "user-2",
                            new UpdateRoleRequest(MemberRole.MODERATOR)
                    );

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void kickMember_returns204() {
        doNothing().when(membershipService)
                .kickMember("mod","room-1","user-2");

        try(MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId)
                    .thenReturn("mod");

            ResponseEntity<Void> response =
                    membershipController.kickMember(
                            "room-1",
                            "user-2"
                    );

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Test
    void getMemberCount_returns200() {
        when(membershipService.getMemberCount("room-1"))
                .thenReturn(5);

        ResponseEntity<Integer> response =
                membershipController.getMemberCount("room-1");

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isEqualTo(5);
    }
}