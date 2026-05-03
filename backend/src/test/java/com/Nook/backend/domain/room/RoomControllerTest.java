package com.Nook.backend.domain.room;

import com.Nook.backend.domain.membership.MemberRole;
import com.Nook.backend.domain.membership.MemberStatus;
import com.Nook.backend.domain.membership.dto.MemberResponse;
import com.Nook.backend.domain.room.dto.CreateRoomRequest;
import com.Nook.backend.domain.room.dto.RoomResponse;
import com.Nook.backend.domain.room.dto.UpdateRoomRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.Nook.backend.auth.SecurityUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock RoomService roomService;
    @InjectMocks RoomController roomController;

    private RoomResponse sampleRoom() {
        return new RoomResponse(
                "room-1",
                "Test Room",
                "desc",
                "ABC123",
                10,
                false,
                "testuser",
                "2026-01-01",
                1,
                List.of()
        );
    }

    @Test
    void getPublicRooms_returns200() {
        when(roomService.getPublicRooms(0, 10))
                .thenReturn(new PageImpl<>(List.of(sampleRoom()), PageRequest.of(0, 10), 1));

        ResponseEntity<?> response = roomController.getPublicRooms(0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getRoom_returns200() {
        when(roomService.getRoom("room-1")).thenReturn(sampleRoom());

        ResponseEntity<RoomResponse> response = roomController.getRoom("room-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Test Room");
    }

    @Test
    void createRoom_returns201() {
        CreateRoomRequest request = new CreateRoomRequest("Test Room", "desc", 10, false);
        when(roomService.createRoom(any(), any())).thenReturn(sampleRoom());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<RoomResponse> response = roomController.createRoom(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Test
    void updateRoom_returns200() {
        UpdateRoomRequest request = new UpdateRoomRequest("New Name", "New Desc", 20, false);
        when(roomService.updateRoom(any(), eq("room-1"), any())).thenReturn(sampleRoom());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<RoomResponse> response = roomController.updateRoom("room-1", request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void deleteRoom_returns204() {
        doNothing().when(roomService).deleteRoom(any(), any());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<Void> response = roomController.deleteRoom("room-1");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Test
    void joinByCode_returns200() {
        when(roomService.joinByCode(any(), eq("ABC123"))).thenReturn(sampleRoom());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<RoomResponse> response = roomController.joinByCode("ABC123");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void getMembers_returns200() {
        when(roomService.getMembers(eq("room-1"), any()))
                .thenReturn(List.of(new MemberResponse("user-1", "testuser", "default",
                        MemberRole.OWNER, MemberStatus.IDLE)));

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<List<MemberResponse>> response = roomController.getMembers("room-1");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    @Test
    void getMyRooms_returns200() {
        when(roomService.getMyRooms(any(), eq(0), eq(5)))
                .thenReturn(new PageImpl<>(List.of(sampleRoom()), PageRequest.of(0, 5), 1));

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<?> response = roomController.getMyRooms(0, 5);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void getRoomByCode_returns200() {
        when(roomService.getRoomByCode("ABC123")).thenReturn(sampleRoom());

        ResponseEntity<RoomResponse> response = roomController.getRoomByCode("ABC123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().roomCode()).isEqualTo("ABC123");
    }

    @Test
    void getAllRooms_returns200() {
        when(roomService.getAllRooms()).thenReturn(List.of(sampleRoom()));

        ResponseEntity<List<RoomResponse>> response = roomController.getAllRooms();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }
}