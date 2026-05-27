package com.Nook.backend.domain.membership.dto;

import com.Nook.backend.domain.membership.MemberRole;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "Role is required")
        MemberRole role
) {}