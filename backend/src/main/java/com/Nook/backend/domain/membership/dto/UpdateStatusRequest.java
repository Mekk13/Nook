package com.Nook.backend.domain.membership.dto;

import com.Nook.backend.domain.membership.MemberStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Status is required")
        MemberStatus status
) {}