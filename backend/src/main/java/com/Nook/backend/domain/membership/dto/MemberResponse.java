package com.Nook.backend.domain.membership.dto;

import com.Nook.backend.domain.membership.MemberRole;
import com.Nook.backend.domain.membership.MemberStatus;

public record MemberResponse(
        String userId,
        String username,
        String avatar,
        MemberRole role,
        MemberStatus status
) {}