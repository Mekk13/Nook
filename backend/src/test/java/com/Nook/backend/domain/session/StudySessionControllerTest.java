package com.Nook.backend.domain.session;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.session.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudySessionControllerTest {

    @Mock StudySessionService sessionService;
    @InjectMocks StudySessionController sessionController;

    private SessionResponse sampleSession() {
        return new SessionResponse(
                "s1",
                "user-1",
                "room-1",
                "Math Study",
                "Math",
                "2026-01-01T10:00",
                null,
                true,
                0.0,          // added durationHours
                List.of()
        );
    }

    private SessionStatsResponse sampleStats() {
        return new SessionStatsResponse(60, 60, 60, 60, 1, 1, 1, 1);
    }

    @Test
    void startSession_returns201() {
        StartSessionRequest request = new StartSessionRequest("room-1");
        when(sessionService.startSession(any(), any())).thenReturn(sampleSession());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<SessionResponse> response = sessionController.startSession(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().id()).isEqualTo("s1");
        }
    }

    @Test
    void endSession_returns200() {
        EndSessionRequest request = new EndSessionRequest("Math Study", "Math");
        when(sessionService.endSession(any(), eq("s1"), any())).thenReturn(sampleSession());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<SessionResponse> response = sessionController.endSession("s1", request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void startBreak_returns200() {
        when(sessionService.startBreak(any(), eq("s1"))).thenReturn(sampleSession());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<SessionResponse> response = sessionController.startBreak("s1");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void endBreak_returns200() {
        when(sessionService.endBreak(any(), eq("s1"))).thenReturn(sampleSession());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<SessionResponse> response = sessionController.endBreak("s1");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void getMyStats_returns200() {
        when(sessionService.getMyStats(any())).thenReturn(sampleStats());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<SessionStatsResponse> response = sessionController.getMyStats();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().totalSessions()).isEqualTo(1);
        }
    }

    @Test
    void getMySessions_returns200() {
        when(sessionService.getMySessions(any(), eq(0), eq(10)))
                .thenReturn(new PageImpl<>(List.of(sampleSession()), PageRequest.of(0, 10), 1));

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<?> response = sessionController.getMySessions(0, 10);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void getSession_returns200() {
        when(sessionService.getSession(any(), eq("s1"))).thenReturn(sampleSession());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<SessionResponse> response = sessionController.getSession("s1");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void deleteSession_returns204() {
        doNothing().when(sessionService).deleteSession(any(), any());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<Void> response = sessionController.deleteSession("s1");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Test
    void getRoomSessions_returns200() {
        when(sessionService.getRoomSessions(eq("room-1"), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of(sampleSession()), PageRequest.of(0, 20), 1));

        ResponseEntity<?> response = sessionController.getRoomSessions("room-1", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void forceEndActiveSession_returns200() {
        doNothing().when(sessionService).forceEndActiveSession(any());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<Void> response =
                    sessionController.forceEndActiveSession("Bearer token");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(sessionService).forceEndActiveSession("user-1");
        }
    }

    @Test
    void updateSession_returns200() {
        UpdateSessionRequest request = new UpdateSessionRequest("New name", "Physics");

        when(sessionService.updateSession(any(), eq("s1"), any()))
                .thenReturn(sampleSession());

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(SecurityUtils::getCurrentUserId).thenReturn("user-1");

            ResponseEntity<SessionResponse> response =
                    sessionController.updateSession("s1", request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }
}