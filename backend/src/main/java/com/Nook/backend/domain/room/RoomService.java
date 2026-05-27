package com.Nook.backend.domain.room;

import com.Nook.backend.domain.log.LoggingService;
import com.Nook.backend.domain.membership.*;
import com.Nook.backend.domain.membership.dto.MemberResponse;
import com.Nook.backend.domain.room.dto.*;
import com.Nook.backend.domain.user.IUserRepository;
import com.Nook.backend.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final IRoomRepository roomRepository;
    private final IMembershipRepository membershipRepository;
    private final IUserRepository userRepository;
    private final LoggingService loggingService;

    private List<MemberResponse> resolveParticipants(String roomId) {
        return membershipRepository.findByRoomId(roomId).stream()
                .map(m -> userRepository.findById(m.getUserId())
                        .map(u -> new MemberResponse(u.getId(), u.getUsername(), u.getAvatar(), m.getRole(), m.getStatus()))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
    private String resolveCreatorName(String creatorId) {
        return userRepository.findById(creatorId)
                .map(u -> u.getUsername())
                .orElse("Unknown");
    }

    public Page<RoomResponse> getPublicRooms(int page, int size) {
        List<Room> all = roomRepository.findPublic();
        return paginate(all, page, size);
    }

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(r -> RoomResponse.from(r, membershipRepository.countByRoomId(r.getId()), resolveCreatorName(r.getCreatorId()), resolveParticipants(r.getId())))
                .toList();
    }

    public Page<RoomResponse> getMyRooms(String userId, int page, int size) {
        List<Room> rooms = membershipRepository.findByUserId(userId).stream()
                .map(m -> roomRepository.findById(m.getRoomId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return paginate(rooms, page, size);
    }

    public RoomResponse getRoom(String roomId) {
        Room room = findRoomOrThrow(roomId);
        return RoomResponse.from(room, membershipRepository.countByRoomId(roomId), resolveCreatorName(room.getCreatorId()),resolveParticipants(room.getId()));
    }

    public RoomResponse createRoom(String userId, CreateRoomRequest request) {
        Room room = Room.builder()
                .id(UUID.randomUUID().toString())
                .name(request.name())
                .description(request.description())
                .creatorId(userId)
                .roomCode(generateUniqueRoomCode())
                .maxParticipants(request.maxParticipants() != null ? request.maxParticipants() : 10)
                .isPrivate(request.isPrivate() != null && request.isPrivate())
                .createdAt(LocalDateTime.now())
                .build();

        roomRepository.save(room);

        RoomMembership membership = RoomMembership.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .roomId(room.getId())
                .role(MemberRole.OWNER)
                .status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now())
                .build();
        membershipRepository.save(membership);
        loggingService.log(userId, "CREATE_ROOM");
        return RoomResponse.from(room, 1, resolveCreatorName(userId), resolveParticipants(room.getId()));
    }

    public RoomResponse updateRoom(String userId, String roomId, UpdateRoomRequest request) {
        Room room = findRoomOrThrow(roomId);
        assertOwner(userId, roomId);

        if (request.name() != null) room.setName(request.name());
        if (request.description() != null) room.setDescription(request.description());
        if (request.maxParticipants() != null) room.setMaxParticipants(request.maxParticipants());
        if (request.isPrivate() != null) room.setPrivate(request.isPrivate());

        roomRepository.update(room);
        return RoomResponse.from(room, membershipRepository.countByRoomId(roomId), resolveCreatorName(room.getCreatorId()),resolveParticipants(room.getId()));
    }

    public void deleteRoom(String userId, String roomId) {
        findRoomOrThrow(roomId);
        assertOwner(userId, roomId);
        roomRepository.delete(roomId);
    }

    public RoomResponse joinByCode(String userId, String code) {
        Room room = roomRepository.findByRoomCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Room not found for code: " + code));

        if (membershipRepository.existsByUserIdAndRoomId(userId, room.getId())) {
            throw new ConflictException("You are already a member of this room");
        }

        int current = membershipRepository.countByRoomId(room.getId());
        if (current >= room.getMaxParticipants()) {
            throw new ConflictException("Room is full");
        }

        RoomMembership membership = RoomMembership.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .roomId(room.getId())
                .role(MemberRole.MEMBER)
                .status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now())
                .build();
        membershipRepository.save(membership);

        return RoomResponse.from(room, current + 1, resolveCreatorName(room.getCreatorId()), resolveParticipants(room.getId()));
    }

    public RoomResponse getRoomByCode(String code) {
        Room room = roomRepository.findByRoomCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Room not found for code: " + code));
        return RoomResponse.from(room, membershipRepository.countByRoomId(room.getId()), resolveCreatorName(room.getCreatorId()),resolveParticipants(room.getId()));
    }

    private Room findRoomOrThrow(String roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }

    private void assertOwner(String userId, String roomId) {
        membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .filter(m -> m.getRole() == MemberRole.OWNER)
                .orElseThrow(() -> new UnauthorizedException("Only the room owner can perform this action"));
    }

    private String generateUniqueRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rng = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
            code = sb.toString();
        } while (roomRepository.existsByRoomCode(code));
        return code;
    }

    private Page<RoomResponse> paginate(List<Room> rooms, int page, int size) {
        int total = rooms.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<RoomResponse> content = rooms.subList(from, to).stream()
                .map(r -> RoomResponse.from(r, membershipRepository.countByRoomId(r.getId()), resolveCreatorName(r.getCreatorId()), resolveParticipants(r.getId())))
                .toList();
        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }

    public List<MemberResponse> getMembers(String roomId, String requestingUserId) {
        findRoomOrThrow(roomId);
        if (!membershipRepository.existsByUserIdAndRoomId(requestingUserId, roomId)) {
            throw new UnauthorizedException("You are not a member of this room");
        }
        return membershipRepository.findByRoomId(roomId).stream()
                .map(m -> userRepository.findById(m.getUserId())
                        .map(u -> new MemberResponse(
                                u.getId(),
                                u.getUsername(),
                                u.getAvatar(),
                                m.getRole(),
                                m.getStatus()
                        ))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
}