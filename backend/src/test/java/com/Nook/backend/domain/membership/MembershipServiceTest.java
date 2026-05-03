package com.Nook.backend.domain.membership;

import com.Nook.backend.domain.membership.dto.UpdateRoleRequest;
import com.Nook.backend.domain.membership.dto.UpdateStatusRequest;
import com.Nook.backend.domain.room.IRoomRepository;
import com.Nook.backend.domain.room.Room;
import com.Nook.backend.exception.ConflictException;
import com.Nook.backend.exception.NotFoundException;
import com.Nook.backend.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock IMembershipRepository membershipRepository;
    @Mock IRoomRepository roomRepository;
    @InjectMocks MembershipService membershipService;

    // ── helpers ───────────────────────────────────────────────────────────────

    private Room room(int maxParticipants) {
        return Room.builder()
                .id("r1")
                .name("Test Room")
                .creatorId("owner")
                .maxParticipants(maxParticipants)
                .build();
    }

    private RoomMembership membership(String userId, String roomId, MemberRole role) {
        return RoomMembership.builder()
                .id("m-" + userId)
                .userId(userId)
                .roomId(roomId)
                .role(role)
                .status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    private void stubRoomExists() {
        when(roomRepository.findById("r1")).thenReturn(Optional.of(room(10)));
    }

    // ── getRoomMembers ────────────────────────────────────────────────────────

    @Test
    void getRoomMembers_returnsAllMembers() {
        stubRoomExists();
        when(membershipRepository.findByRoomId("r1"))
                .thenReturn(List.of(membership("u1", "r1", MemberRole.MEMBER)));

        assertThat(membershipService.getRoomMembers("r1")).hasSize(1);
    }

    @Test
    void getRoomMembers_roomNotFound_throws() {
        when(roomRepository.findById("r1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.getRoomMembers("r1"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── getMyMemberships ──────────────────────────────────────────────────────

    @Test
    void getMyMemberships_returnsAllMembershipsForUser() {
        when(membershipRepository.findByUserId("u1"))
                .thenReturn(List.of(
                        membership("u1", "r1", MemberRole.MEMBER),
                        membership("u1", "r2", MemberRole.OWNER)
                ));

        assertThat(membershipService.getMyMemberships("u1")).hasSize(2);
    }

    // ── getMembership ─────────────────────────────────────────────────────────

    @Test
    void getMembership_found() {
        when(membershipRepository.findByUserIdAndRoomId("u1", "r1"))
                .thenReturn(Optional.of(membership("u1", "r1", MemberRole.MEMBER)));

        assertThat(membershipService.getMembership("u1", "r1")).isNotNull();
    }

    @Test
    void getMembership_notFound_throws() {
        when(membershipRepository.findByUserIdAndRoomId("u1", "r1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.getMembership("u1", "r1"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── joinRoom ──────────────────────────────────────────────────────────────

    @Test
    void joinRoom_success() {
        stubRoomExists();
        when(membershipRepository.existsByUserIdAndRoomId("u1", "r1")).thenReturn(false);
        when(membershipRepository.countByRoomId("r1")).thenReturn(0);
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = membershipService.joinRoom("u1", "r1");

        assertThat(response.userId()).isEqualTo("u1");
        assertThat(response.roomId()).isEqualTo("r1");
        assertThat(response.role()).isEqualTo(MemberRole.MEMBER);
        verify(membershipRepository).save(any(RoomMembership.class));
    }

    @Test
    void joinRoom_alreadyMember_throws() {
        stubRoomExists();
        when(membershipRepository.existsByUserIdAndRoomId("u1", "r1")).thenReturn(true);

        assertThatThrownBy(() -> membershipService.joinRoom("u1", "r1"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void joinRoom_roomFull_throws() {
        when(roomRepository.findById("r1")).thenReturn(Optional.of(room(2)));
        when(membershipRepository.existsByUserIdAndRoomId("u1", "r1")).thenReturn(false);
        when(membershipRepository.countByRoomId("r1")).thenReturn(2);

        assertThatThrownBy(() -> membershipService.joinRoom("u1", "r1"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void joinRoom_roomNotFound_throws() {
        when(roomRepository.findById("r1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.joinRoom("u1", "r1"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── leaveRoom ─────────────────────────────────────────────────────────────

    @Test
    void leaveRoom_success() {
        stubRoomExists();
        when(membershipRepository.findByUserIdAndRoomId("u1", "r1"))
                .thenReturn(Optional.of(membership("u1", "r1", MemberRole.MEMBER)));

        membershipService.leaveRoom("u1", "r1");

        verify(membershipRepository).delete("u1", "r1");
    }

    @Test
    void leaveRoom_ownerCannotLeave_throws() {
        stubRoomExists();
        when(membershipRepository.findByUserIdAndRoomId("u1", "r1"))
                .thenReturn(Optional.of(membership("u1", "r1", MemberRole.OWNER)));

        assertThatThrownBy(() -> membershipService.leaveRoom("u1", "r1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void leaveRoom_notMember_throws() {
        stubRoomExists();
        when(membershipRepository.findByUserIdAndRoomId("u1", "r1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.leaveRoom("u1", "r1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void leaveRoom_roomNotFound_throws() {
        when(roomRepository.findById("r1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.leaveRoom("u1", "r1"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    void updateStatus_success() {
        RoomMembership m = membership("u1", "r1", MemberRole.MEMBER);
        when(membershipRepository.findByUserIdAndRoomId("u1", "r1")).thenReturn(Optional.of(m));

        var response = membershipService.updateStatus("u1", "r1", new UpdateStatusRequest(MemberStatus.STUDYING));

        assertThat(response.status()).isEqualTo(MemberStatus.STUDYING);
        verify(membershipRepository).update(m);
    }

    @Test
    void updateStatus_notMember_throws() {
        when(membershipRepository.findByUserIdAndRoomId("u1", "r1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.updateStatus("u1", "r1", new UpdateStatusRequest(MemberStatus.STUDYING)))
                .isInstanceOf(NotFoundException.class);
    }

    // ── updateRole ────────────────────────────────────────────────────────────

    @Test
    void updateRole_ownerPromotesToModerator_success() {
        // Caller is OWNER
        when(membershipRepository.findByUserIdAndRoomId("owner", "r1"))
                .thenReturn(Optional.of(membership("owner", "r1", MemberRole.OWNER)));
        // Target is MEMBER
        RoomMembership target = membership("u2", "r1", MemberRole.MEMBER);
        when(membershipRepository.findByUserIdAndRoomId("u2", "r1"))
                .thenReturn(Optional.of(target));

        var response = membershipService.updateRole("owner", "r1", "u2", new UpdateRoleRequest(MemberRole.MODERATOR));

        assertThat(response.role()).isEqualTo(MemberRole.MODERATOR);
        verify(membershipRepository).update(target);
    }

    @Test
    void updateRole_moderatorDemotesToMember_success() {
        // Caller is MODERATOR
        when(membershipRepository.findByUserIdAndRoomId("mod", "r1"))
                .thenReturn(Optional.of(membership("mod", "r1", MemberRole.MODERATOR)));
        // Target is MODERATOR being demoted
        RoomMembership target = membership("u2", "r1", MemberRole.MODERATOR);
        when(membershipRepository.findByUserIdAndRoomId("u2", "r1"))
                .thenReturn(Optional.of(target));

        var response = membershipService.updateRole("mod", "r1", "u2", new UpdateRoleRequest(MemberRole.MEMBER));

        assertThat(response.role()).isEqualTo(MemberRole.MEMBER);
    }

    @Test
    void updateRole_moderatorCannotPromoteToModerator_throws() {
        // Caller is MODERATOR — not allowed to assign MODERATOR role
        when(membershipRepository.findByUserIdAndRoomId("mod", "r1"))
                .thenReturn(Optional.of(membership("mod", "r1", MemberRole.MODERATOR)));
        when(membershipRepository.findByUserIdAndRoomId("u2", "r1"))
                .thenReturn(Optional.of(membership("u2", "r1", MemberRole.MEMBER)));

        assertThatThrownBy(() -> membershipService.updateRole("mod", "r1", "u2", new UpdateRoleRequest(MemberRole.MODERATOR)))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateRole_cannotChangeOwnerRole_throws() {
        when(membershipRepository.findByUserIdAndRoomId("owner", "r1"))
                .thenReturn(Optional.of(membership("owner", "r1", MemberRole.OWNER)));
        when(membershipRepository.findByUserIdAndRoomId("target-owner", "r1"))
                .thenReturn(Optional.of(membership("target-owner", "r1", MemberRole.OWNER)));

        assertThatThrownBy(() -> membershipService.updateRole("owner", "r1", "target-owner", new UpdateRoleRequest(MemberRole.MEMBER)))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateRole_callerNotOwnerOrMod_throws() {
        when(membershipRepository.findByUserIdAndRoomId("u1", "r1"))
                .thenReturn(Optional.of(membership("u1", "r1", MemberRole.MEMBER)));

        assertThatThrownBy(() -> membershipService.updateRole("u1", "r1", "u2", new UpdateRoleRequest(MemberRole.MODERATOR)))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateRole_targetNotFound_throws() {
        when(membershipRepository.findByUserIdAndRoomId("owner", "r1"))
                .thenReturn(Optional.of(membership("owner", "r1", MemberRole.OWNER)));
        when(membershipRepository.findByUserIdAndRoomId("ghost", "r1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.updateRole("owner", "r1", "ghost", new UpdateRoleRequest(MemberRole.MEMBER)))
                .isInstanceOf(NotFoundException.class);
    }

    // ── kickMember ────────────────────────────────────────────────────────────

    @Test
    void kickMember_success() {
        when(membershipRepository.findByUserIdAndRoomId("mod", "r1"))
                .thenReturn(Optional.of(membership("mod", "r1", MemberRole.MODERATOR)));
        when(membershipRepository.findByUserIdAndRoomId("u2", "r1"))
                .thenReturn(Optional.of(membership("u2", "r1", MemberRole.MEMBER)));

        membershipService.kickMember("mod", "r1", "u2");

        verify(membershipRepository).delete("u2", "r1");
    }

    @Test
    void kickMember_cannotKickOwner_throws() {
        when(membershipRepository.findByUserIdAndRoomId("mod", "r1"))
                .thenReturn(Optional.of(membership("mod", "r1", MemberRole.MODERATOR)));
        when(membershipRepository.findByUserIdAndRoomId("owner", "r1"))
                .thenReturn(Optional.of(membership("owner", "r1", MemberRole.OWNER)));

        assertThatThrownBy(() -> membershipService.kickMember("mod", "r1", "owner"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void kickMember_callerNotOwnerOrMod_throws() {
        when(membershipRepository.findByUserIdAndRoomId("u1", "r1"))
                .thenReturn(Optional.of(membership("u1", "r1", MemberRole.MEMBER)));

        assertThatThrownBy(() -> membershipService.kickMember("u1", "r1", "u2"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void kickMember_targetNotFound_throws() {
        when(membershipRepository.findByUserIdAndRoomId("owner", "r1"))
                .thenReturn(Optional.of(membership("owner", "r1", MemberRole.OWNER)));
        when(membershipRepository.findByUserIdAndRoomId("ghost", "r1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.kickMember("owner", "r1", "ghost"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── getMemberCount ────────────────────────────────────────────────────────

    @Test
    void getMemberCount_returnsCount() {
        stubRoomExists();
        when(membershipRepository.countByRoomId("r1")).thenReturn(3);

        assertThat(membershipService.getMemberCount("r1")).isEqualTo(3);
    }

    @Test
    void getMemberCount_roomNotFound_throws() {
        when(roomRepository.findById("r1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.getMemberCount("r1"))
                .isInstanceOf(NotFoundException.class);
    }
}