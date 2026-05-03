package com.Nook.backend.domain.room.dto;

import com.Nook.backend.domain.membership.dto.MemberResponse;
import com.Nook.backend.domain.room.Room;

import java.util.List;

public record RoomResponse(
        String id,
        String name,
        String description,
        String roomCode,
        int maxParticipants,
        boolean isPrivate,
        String creatorName,
        String createdAt,
        int memberCount,
        List<MemberResponse> participants
) {
    public static RoomResponse from(Room room, int memberCount, String creatorName, List<MemberResponse> participants) {
        return new RoomResponse(
                room.getId(), room.getName(), room.getDescription(),
                room.getRoomCode(), room.getMaxParticipants(), room.isPrivate(),
                creatorName, room.getCreatedAt().toString(), memberCount, participants
        );
    }
}