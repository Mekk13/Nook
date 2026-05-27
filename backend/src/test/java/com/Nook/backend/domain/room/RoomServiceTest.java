package com.Nook.backend.domain.room;

import com.Nook.backend.domain.membership.IMembershipRepository;
import com.Nook.backend.domain.membership.MemberRole;
import com.Nook.backend.domain.membership.MemberStatus;
import com.Nook.backend.domain.membership.RoomMembership;
import com.Nook.backend.domain.room.dto.CreateRoomRequest;
import com.Nook.backend.domain.room.dto.RoomResponse;
import com.Nook.backend.domain.room.dto.UpdateRoomRequest;
import com.Nook.backend.domain.user.IUserRepository;
import com.Nook.backend.domain.user.User;
import com.Nook.backend.exception.ConflictException;
import com.Nook.backend.exception.NotFoundException;
import com.Nook.backend.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock private IRoomRepository roomRepository;
    @Mock private IMembershipRepository membershipRepository;
    @Mock private IUserRepository userRepository;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom;
    private User testUser;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .id("room-1")
                .name("Test Room")
                .creatorId("user-1")
                .roomCode("ABC123")
                .maxParticipants(10)
                .isPrivate(false)
                .createdAt(LocalDateTime.now())
                .build();

        testUser = User.builder()
                .id("user-1")
                .username("testuser")
                .email("test@test.com")
                .avatar("default")
                .build();
    }

    @Test
    void getRoom_returnsRoom_whenExists() {
        when(roomRepository.findById("room-1")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.countByRoomId("room-1")).thenReturn(1);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

        RoomResponse response = roomService.getRoom("room-1");

        assertThat(response.id()).isEqualTo("room-1");
    }

    @Test
    void getRoom_throwsNotFoundException_whenNotExists() {
        when(roomRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoom("bad-id"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getPublicRooms_returnsOnlyPublicRooms() {
        when(roomRepository.findPublic()).thenReturn(List.of(testRoom));
        when(membershipRepository.countByRoomId(any())).thenReturn(1);
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));

        Page<RoomResponse> result = roomService.getPublicRooms(0, 5);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void deleteRoom_deletesRoom_whenOwner() {
        RoomMembership ownership = RoomMembership.builder()
                .userId("user-1").roomId("room-1")
                .role(MemberRole.OWNER).status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now()).build();

        when(roomRepository.findById("room-1")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.findByUserIdAndRoomId("user-1", "room-1"))
                .thenReturn(Optional.of(ownership));

        roomService.deleteRoom("user-1", "room-1");

        verify(roomRepository).delete("room-1");
    }

    @Test
    void deleteRoom_throwsUnauthorized_whenNotOwner() {
        RoomMembership membership = RoomMembership.builder()
                .userId("user-1").roomId("room-1")
                .role(MemberRole.MEMBER).status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now()).build();

        when(roomRepository.findById("room-1")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.findByUserIdAndRoomId("user-1", "room-1"))
                .thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> roomService.deleteRoom("user-1", "room-1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateRoom_updatesFields_whenOwner() {
        RoomMembership ownership = RoomMembership.builder()
                .userId("user-1").roomId("room-1")
                .role(MemberRole.OWNER).status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now()).build();
        UpdateRoomRequest request = new UpdateRoomRequest("New Name", "New Desc", 20, false);

        when(roomRepository.findById("room-1")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.findByUserIdAndRoomId("user-1", "room-1"))
                .thenReturn(Optional.of(ownership));
        when(membershipRepository.countByRoomId("room-1")).thenReturn(1);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

        RoomResponse response = roomService.updateRoom("user-1", "room-1", request);

        assertThat(response.name()).isEqualTo("New Name");
        verify(roomRepository).update(any());
    }

    @Test
    void joinByCode_addsMembership_whenValid() {
        when(roomRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.existsByUserIdAndRoomId("user-2", "room-1")).thenReturn(false);
        when(membershipRepository.countByRoomId("room-1")).thenReturn(1);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

        roomService.joinByCode("user-2", "ABC123");

        verify(membershipRepository).save(any());
    }

    @Test
    void joinByCode_throwsConflict_whenAlreadyMember() {
        when(roomRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.existsByUserIdAndRoomId("user-1", "room-1")).thenReturn(true);

        assertThatThrownBy(() -> roomService.joinByCode("user-1", "ABC123"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void joinByCode_throwsConflict_whenRoomFull() {
        when(roomRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.existsByUserIdAndRoomId("user-2", "room-1")).thenReturn(false);
        when(membershipRepository.countByRoomId("room-1")).thenReturn(10); // full

        assertThatThrownBy(() -> roomService.joinByCode("user-2", "ABC123"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getMyRooms_returnsRoomsForUser() {
        RoomMembership membership = RoomMembership.builder()
                .userId("user-1").roomId("room-1")
                .role(MemberRole.MEMBER).status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now()).build();

        when(membershipRepository.findByUserId("user-1")).thenReturn(List.of(membership));
        when(roomRepository.findById("room-1")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.countByRoomId("room-1")).thenReturn(1);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

        Page<RoomResponse> result = roomService.getMyRooms("user-1", 0, 5);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void createRoom_savesRoomAndMembership() {
        CreateRoomRequest request = new CreateRoomRequest("Test Room", "desc", 10, false);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
        when(roomRepository.existsByRoomCode(any())).thenReturn(false);

        RoomResponse response = roomService.createRoom("user-1", request);

        assertThat(response.name()).isEqualTo("Test Room");
        verify(roomRepository).save(any());
        verify(membershipRepository).save(any());
    }

    @Test
    void getAllRooms_returnsAllRooms() {
        when(roomRepository.findAll()).thenReturn(List.of(testRoom));
        when(membershipRepository.countByRoomId(any())).thenReturn(1);
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));

        List<RoomResponse> result = roomService.getAllRooms();

        assertThat(result).hasSize(1);
    }

    @Test
    void getRoomByCode_returnsRoom_whenExists() {
        when(roomRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.countByRoomId("room-1")).thenReturn(1);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

        RoomResponse response = roomService.getRoomByCode("ABC123");

        assertThat(response.roomCode()).isEqualTo("ABC123");
    }

    @Test
    void getRoomByCode_throwsNotFoundException_whenNotExists() {
        when(roomRepository.findByRoomCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomByCode("NOPE"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getMembers_returnsMembers_whenRequesterIsMember() {
        RoomMembership membership = RoomMembership.builder()
                .userId("user-1").roomId("room-1")
                .role(MemberRole.OWNER).status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now()).build();

        when(roomRepository.findById("room-1")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.existsByUserIdAndRoomId("user-1", "room-1")).thenReturn(true);
        when(membershipRepository.findByRoomId("room-1")).thenReturn(List.of(membership));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

        var result = roomService.getMembers("room-1", "user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("testuser");
    }

    @Test
    void getMembers_throwsUnauthorized_whenRequesterNotMember() {
        when(roomRepository.findById("room-1")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.existsByUserIdAndRoomId("user-2", "room-1")).thenReturn(false);

        assertThatThrownBy(() -> roomService.getMembers("room-1", "user-2"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateRoom_throwsUnauthorized_whenNotOwner() {
        RoomMembership membership = RoomMembership.builder()
                .userId("user-1").roomId("room-1")
                .role(MemberRole.MEMBER).status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now()).build();
        UpdateRoomRequest request = new UpdateRoomRequest("New Name", "New Desc", 20, false);

        when(roomRepository.findById("room-1")).thenReturn(Optional.of(testRoom));
        when(membershipRepository.findByUserIdAndRoomId("user-1", "room-1"))
                .thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> roomService.updateRoom("user-1", "room-1", request))
                .isInstanceOf(UnauthorizedException.class);
    }
}