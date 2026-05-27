package com.Nook.backend.domain.room;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.membership.dto.MemberResponse;
import com.Nook.backend.domain.room.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // Public — no auth needed (matches SecurityConfig)
    @GetMapping
    public ResponseEntity<Page<RoomResponse>> getPublicRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(roomService.getPublicRooms(page, size));
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    // IMPORTANT: /my must be declared before /{id}
    // otherwise Spring matches "my" as the {id} path variable
    @GetMapping("/my")
    public ResponseEntity<Page<RoomResponse>> getMyRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(roomService.getMyRooms(userId, page, size));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<RoomResponse> getRoomByCode(@PathVariable String code) {
        return ResponseEntity.ok(roomService.getRoomByCode(code));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable String id) {
        return ResponseEntity.ok(roomService.getRoom(id));
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(roomService.createRoom(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoomRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(roomService.updateRoom(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        roomService.deleteRoom(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<RoomResponse> joinByCode(@PathVariable String code) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(roomService.joinByCode(userId, code));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(roomService.getMembers(id, userId));
    }
}