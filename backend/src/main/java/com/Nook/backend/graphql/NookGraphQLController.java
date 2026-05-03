package com.Nook.backend.graphql;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.membership.dto.MembershipResponse;
import com.Nook.backend.domain.room.RoomService;
import com.Nook.backend.domain.room.dto.CreateRoomRequest;
import com.Nook.backend.domain.room.dto.RoomResponse;
import com.Nook.backend.domain.room.dto.UpdateRoomRequest;
import com.Nook.backend.domain.session.StudySessionService;
import com.Nook.backend.domain.session.dto.*;
import com.Nook.backend.domain.membership.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NookGraphQLController {

    private final RoomService roomService;
    private final StudySessionService sessionService;
    private final MembershipService membershipService;

    // ─── Room Queries ─────────────────────────────────────────────────────────

    @QueryMapping
    public Map<String, Object> myRooms(@Argument Integer page, @Argument Integer size) {
        String userId = SecurityUtils.getCurrentUserId();
        Page<RoomResponse> result = roomService.getMyRooms(userId, page != null ? page : 0, size != null ? size : 5);
        return Map.of(
                "content", result.getContent(),
                "totalPages", result.getTotalPages(),
                "totalElements", result.getTotalElements()
        );
    }

    @QueryMapping
    public Map<String, Object> publicRooms(@Argument Integer page, @Argument Integer size) {
        Page<RoomResponse> result = roomService.getPublicRooms(page != null ? page : 0, size != null ? size : 5);
        return Map.of(
                "content", result.getContent(),
                "totalPages", result.getTotalPages(),
                "totalElements", result.getTotalElements()
        );
    }

    @QueryMapping
    public RoomResponse room(@Argument String id) {
        return roomService.getRoom(id);
    }

    // ─── Session Queries ──────────────────────────────────────────────────────

    @QueryMapping
    public Map<String, Object> mySessions(@Argument Integer page, @Argument Integer size) {
        String userId = SecurityUtils.getCurrentUserId();
        Page<SessionResponse> result = sessionService.getMySessions(userId, page != null ? page : 0, size != null ? size : 10);
        return Map.of(
                "content", result.getContent(),
                "totalPages", result.getTotalPages(),
                "totalElements", result.getTotalElements()
        );
    }

    @QueryMapping
    public Map<String, Object> roomSessions(@Argument String roomId, @Argument Integer page, @Argument Integer size) {
        Page<SessionResponse> result = sessionService.getRoomSessions(roomId, page != null ? page : 0, size != null ? size : 20);
        return Map.of(
                "content", result.getContent(),
                "totalPages", result.getTotalPages(),
                "totalElements", result.getTotalElements()
        );
    }

    @QueryMapping
    public SessionResponse session(@Argument String id) {
        String userId = SecurityUtils.getCurrentUserId();
        return sessionService.getSession(userId, id);
    }

    @QueryMapping
    public SessionStatsResponse myStats() {
        String userId = SecurityUtils.getCurrentUserId();
        return sessionService.getMyStats(userId);
    }

    // ─── Member Queries ───────────────────────────────────────────────────────

    @QueryMapping
    public List<MembershipResponse> roomMembers(@Argument String roomId) {
        return membershipService.getRoomMembers(roomId);
    }

    // ─── Room Mutations ───────────────────────────────────────────────────────

    @MutationMapping
    public RoomResponse createRoom(@Argument String name, @Argument String description,
                                   @Argument Integer maxParticipants, @Argument Boolean isPrivate) {
        String userId = SecurityUtils.getCurrentUserId();
        return roomService.createRoom(userId, new CreateRoomRequest(name, description, maxParticipants, isPrivate));
    }

    @MutationMapping
    public RoomResponse updateRoom(@Argument String id, @Argument String name, @Argument String description,
                                   @Argument Integer maxParticipants, @Argument Boolean isPrivate) {
        String userId = SecurityUtils.getCurrentUserId();
        return roomService.updateRoom(userId, id, new UpdateRoomRequest(name, description, maxParticipants, isPrivate));
    }

    @MutationMapping
    public Boolean deleteRoom(@Argument String id) {
        String userId = SecurityUtils.getCurrentUserId();
        roomService.deleteRoom(userId, id);
        return true;
    }

    @MutationMapping
    public RoomResponse joinRoomByCode(@Argument String code) {
        String userId = SecurityUtils.getCurrentUserId();
        return roomService.joinByCode(userId, code);
    }

    // ─── Session Mutations ────────────────────────────────────────────────────

    @MutationMapping
    public SessionResponse startSession(@Argument String roomId) {
        String userId = SecurityUtils.getCurrentUserId();
        return sessionService.startSession(userId, new StartSessionRequest(roomId));
    }

    @MutationMapping
    public SessionResponse endSession(@Argument String id, @Argument String name, @Argument String subject) {
        String userId = SecurityUtils.getCurrentUserId();
        return sessionService.endSession(userId, id, new EndSessionRequest(name, subject));
    }

    @MutationMapping
    public SessionResponse startBreak(@Argument String id) {
        String userId = SecurityUtils.getCurrentUserId();
        return sessionService.startBreak(userId, id);
    }

    @MutationMapping
    public SessionResponse endBreak(@Argument String id) {
        String userId = SecurityUtils.getCurrentUserId();
        return sessionService.endBreak(userId, id);
    }

    @MutationMapping
    public Boolean deleteSession(@Argument String id) {
        String userId = SecurityUtils.getCurrentUserId();
        sessionService.deleteSession(userId, id);
        return true;
    }
}