package com.Nook.backend.domain.membership;

import com.Nook.backend.domain.membership.dto.*;
import com.Nook.backend.domain.room.IRoomRepository;
import com.Nook.backend.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final IMembershipRepository membershipRepository;
    private final IRoomRepository roomRepository;

    public List<MembershipResponse> getRoomMembers(String roomId) {
        assertRoomExists(roomId);
        return membershipRepository.findByRoomId(roomId).stream()
                .map(MembershipResponse::from)
                .toList();
    }

    public List<MembershipResponse> getMyMemberships(String userId) {
        return membershipRepository.findByUserId(userId).stream()
                .map(MembershipResponse::from)
                .toList();
    }

    public MembershipResponse getMembership(String userId, String roomId) {
        return membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .map(MembershipResponse::from)
                .orElseThrow(() -> new NotFoundException("Membership not found"));
    }

    public MembershipResponse joinRoom(String userId, String roomId) {
        assertRoomExists(roomId);

        if (membershipRepository.existsByUserIdAndRoomId(userId, roomId)) {
            throw new ConflictException("You are already a member of this room");
        }

        var room = roomRepository.findById(roomId).get();
        int current = membershipRepository.countByRoomId(roomId);
        if (current >= room.getMaxParticipants()) {
            throw new ConflictException("Room is full");
        }

        RoomMembership membership = RoomMembership.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .roomId(roomId)
                .role(MemberRole.MEMBER)
                .status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now())  // fixed
                .build();

        membershipRepository.save(membership);
        return MembershipResponse.from(membership);
    }

    public void leaveRoom(String userId, String roomId) {
        assertRoomExists(roomId);

        RoomMembership membership = membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .orElseThrow(() -> new NotFoundException("You are not a member of this room"));

        if (membership.getRole() == MemberRole.OWNER) {
            throw new UnauthorizedException("Owner cannot leave — delete the room or transfer ownership first");
        }

        membershipRepository.delete(userId, roomId);
    }

    public MembershipResponse updateStatus(String userId, String roomId, UpdateStatusRequest request) {
        RoomMembership membership = membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .orElseThrow(() -> new NotFoundException("You are not a member of this room"));

        membership.setStatus(request.status());
        membershipRepository.update(membership);
        return MembershipResponse.from(membership);
    }

    public MembershipResponse updateRole(String userId, String roomId, String targetUserId, UpdateRoleRequest request) {
        assertOwnerOrModerator(userId, roomId);

        RoomMembership target = membershipRepository.findByUserIdAndRoomId(targetUserId, roomId)
                .orElseThrow(() -> new NotFoundException("Target user is not a member of this room"));

        if (target.getRole() == MemberRole.OWNER) {
            throw new UnauthorizedException("Cannot change the role of the room owner");
        }

        if (request.role() == MemberRole.MODERATOR) {
            assertOwner(userId, roomId);
        }

        target.setRole(request.role());
        membershipRepository.update(target);
        return MembershipResponse.from(target);
    }

    public void kickMember(String userId, String roomId, String targetUserId) {
        assertOwnerOrModerator(userId, roomId);

        RoomMembership target = membershipRepository.findByUserIdAndRoomId(targetUserId, roomId)
                .orElseThrow(() -> new NotFoundException("Target user is not a member of this room"));

        if (target.getRole() == MemberRole.OWNER) {
            throw new UnauthorizedException("Cannot kick the room owner");
        }

        membershipRepository.delete(targetUserId, roomId);
    }

    public int getMemberCount(String roomId) {
        assertRoomExists(roomId);
        return membershipRepository.countByRoomId(roomId);
    }

    private void assertRoomExists(String roomId) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }

    private void assertOwner(String userId, String roomId) {
        membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .filter(m -> m.getRole() == MemberRole.OWNER)
                .orElseThrow(() -> new UnauthorizedException("Only the room owner can perform this action"));
    }

    private void assertOwnerOrModerator(String userId, String roomId) {
        membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .filter(m -> m.getRole() == MemberRole.OWNER || m.getRole() == MemberRole.MODERATOR)
                .orElseThrow(() -> new UnauthorizedException("Only owners and moderators can perform this action"));
    }
}