package com.Nook.backend.domain.membership;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomMembership {

    private String id;
    private String userId;
    private String roomId;
    @Builder.Default
    private MemberStatus status = MemberStatus.IDLE;
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;
    private LocalDateTime joinedAt;
}