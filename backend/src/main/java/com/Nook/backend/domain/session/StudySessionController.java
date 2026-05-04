package com.Nook.backend.domain.session;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.session.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<SessionResponse> startSession(
            @Valid @RequestBody StartSessionRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(sessionService.startSession(userId, request));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<SessionResponse> endSession(
            @PathVariable String id,
            @Valid @RequestBody EndSessionRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.endSession(userId, id, request));
    }

    @PostMapping("/{id}/break/start")
    public ResponseEntity<SessionResponse> startBreak(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.startBreak(userId, id));
    }

    @PostMapping("/{id}/break/end")
    public ResponseEntity<SessionResponse> endBreak(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.endBreak(userId, id));
    }

    // IMPORTANT: /my/stats must be declared BEFORE /my
    // otherwise Spring matches "stats" as the {id} path variable in /{id}
    @GetMapping("/my/stats")
    public ResponseEntity<SessionStatsResponse> getMyStats() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.getMyStats(userId));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<SessionResponse>> getMySessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.getMySessions(userId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.getSession(userId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        sessionService.deleteSession(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Page<SessionResponse>> getRoomSessions(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(sessionService.getRoomSessions(roomId, page, size));
    }

    @PostMapping("/force-end")
    public ResponseEntity<Void> forceEndActiveSession(@RequestHeader("Authorization") String authHeader) {
        String userId = SecurityUtils.getCurrentUserId();
        sessionService.forceEndActiveSession(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SessionResponse> updateSession(
            @PathVariable String id,
            @RequestBody UpdateSessionRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.updateSession(userId, id, request));
    }

    @GetMapping("/my/filter")
    public ResponseEntity<Page<SessionResponse>> getMySessionsBySubject(
            @RequestParam String subject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.getMySessionsBySubject(userId, subject, page, size));
    }

    @GetMapping("/my/subjects")
    public ResponseEntity<List<String>> getMySubjects() {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(sessionService.getMySubjects(userId));
    }
}