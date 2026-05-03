package com.Nook.backend.domain.membership;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.membership.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms/{roomId}")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    // GET /api/rooms/{roomId}/members/count
    // Declared before /members/me and /members/{targetUserId} to avoid path conflicts
    @GetMapping("/members/count")
    public ResponseEntity<Integer> getMemberCount(@PathVariable String roomId) {
        return ResponseEntity.ok(membershipService.getMemberCount(roomId));
    }

    // GET /api/rooms/{roomId}/members/me
    @GetMapping("/members/me")
    public ResponseEntity<MembershipResponse> getMyMembership(@PathVariable String roomId) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(membershipService.getMembership(userId, roomId));
    }

    // POST /api/rooms/{roomId}/join
    @PostMapping("/join")
    public ResponseEntity<MembershipResponse> joinRoom(@PathVariable String roomId) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(membershipService.joinRoom(userId, roomId));
    }

    // DELETE /api/rooms/{roomId}/leave
    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable String roomId) {
        String userId = SecurityUtils.getCurrentUserId();
        membershipService.leaveRoom(userId, roomId);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/rooms/{roomId}/status
    @PatchMapping("/status")
    public ResponseEntity<MembershipResponse> updateStatus(
            @PathVariable String roomId,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(membershipService.updateStatus(userId, roomId, request));
    }

    // PATCH /api/rooms/{roomId}/members/{targetUserId}/role
    @PatchMapping("/members/{targetUserId}/role")
    public ResponseEntity<MembershipResponse> updateRole(
            @PathVariable String roomId,
            @PathVariable String targetUserId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(membershipService.updateRole(userId, roomId, targetUserId, request));
    }

    // DELETE /api/rooms/{roomId}/members/{targetUserId}/kick
    @DeleteMapping("/members/{targetUserId}/kick")
    public ResponseEntity<Void> kickMember(
            @PathVariable String roomId,
            @PathVariable String targetUserId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        membershipService.kickMember(userId, roomId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<MembershipResponse>> getRoomMembers(@PathVariable String roomId) {
        return ResponseEntity.ok(membershipService.getRoomMembers(roomId));
    }
}