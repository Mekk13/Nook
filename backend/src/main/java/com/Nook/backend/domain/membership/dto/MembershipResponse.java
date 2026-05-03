package com.Nook.backend.domain.membership.dto;

import com.Nook.backend.domain.membership.MemberRole;
import com.Nook.backend.domain.membership.MemberStatus;
import com.Nook.backend.domain.membership.RoomMembership;

public record MembershipResponse(
        String id,
        String userId,
        String roomId,
        MemberRole role,
        MemberStatus status,
        String joinedAt
) {
    public static MembershipResponse from(RoomMembership m) {
        return new MembershipResponse(
                m.getId(), m.getUserId(), m.getRoomId(),
                m.getRole(), m.getStatus(), m.getJoinedAt().toString()
        );
    }
}