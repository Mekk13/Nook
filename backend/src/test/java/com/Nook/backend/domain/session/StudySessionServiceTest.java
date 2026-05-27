package com.Nook.backend.domain.session;

import com.Nook.backend.domain.membership.IMembershipRepository;
import com.Nook.backend.domain.session.dto.*;
import com.Nook.backend.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock IStudySessionRepository sessionRepository;
    @Mock IMembershipRepository membershipRepository;

    @InjectMocks StudySessionService sessionService;

    private StudySession activeSession;
    private StudySession endedSession;

    @BeforeEach
    void setUp() {
        activeSession = StudySession.builder()
                .id("s1").userId("user-1").roomId("room-1")
                .startedAt(LocalDateTime.now().minusHours(1))
                .breaks(new ArrayList<>())
                .build();

        endedSession = StudySession.builder()
                .id("s2").userId("user-1").roomId("room-1")
                .startedAt(LocalDateTime.now().minusHours(2))
                .endedAt(LocalDateTime.now().minusHours(1))
                .name("Math Study").subject("Math")
                .breaks(new ArrayList<>())
                .build();
    }

    // --- startSession ---

    @Test
    void startSession_savesSession_whenValid() {
        StartSessionRequest request = new StartSessionRequest("room-1");
        when(membershipRepository.existsByUserIdAndRoomId("user-1", "room-1")).thenReturn(true);
        when(sessionRepository.findActiveByUserId("user-1")).thenReturn(Optional.empty());

        SessionResponse response = sessionService.startSession("user-1", request);

        assertThat(response.roomId()).isEqualTo("room-1");
        assertThat(response.inProgress()).isTrue();
        verify(sessionRepository).save(any());
    }

    @Test
    void startSession_throwsUnauthorized_whenNotMember() {
        StartSessionRequest request = new StartSessionRequest("room-1");
        when(membershipRepository.existsByUserIdAndRoomId("user-1", "room-1")).thenReturn(false);

        assertThatThrownBy(() -> sessionService.startSession("user-1", request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void startSession_throwsConflict_whenAlreadyActive() {
        StartSessionRequest request = new StartSessionRequest("room-1");
        when(membershipRepository.existsByUserIdAndRoomId("user-1", "room-1")).thenReturn(true);
        when(sessionRepository.findActiveByUserId("user-1")).thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> sessionService.startSession("user-1", request))
                .isInstanceOf(ConflictException.class);
    }

    // --- endSession ---

    @Test
    void endSession_endsSession_whenValid() {
        EndSessionRequest request = new EndSessionRequest("Math Study", "Math");
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        SessionResponse response = sessionService.endSession("user-1", "s1", request);

        assertThat(response.inProgress()).isFalse();
        verify(sessionRepository).update(any());
    }

    @Test
    void endSession_throwsConflict_whenAlreadyEnded() {
        EndSessionRequest request = new EndSessionRequest("Math Study", "Math");
        when(sessionRepository.findById("s2")).thenReturn(Optional.of(endedSession));

        assertThatThrownBy(() -> sessionService.endSession("user-1", "s2", request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void endSession_throwsUnauthorized_whenNotOwner() {
        EndSessionRequest request = new EndSessionRequest("Math Study", "Math");
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> sessionService.endSession("user-2", "s1", request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void endSession_throwsNotFound_whenSessionMissing() {
        EndSessionRequest request = new EndSessionRequest("Math Study", "Math");
        when(sessionRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.endSession("user-1", "bad", request))
                .isInstanceOf(NotFoundException.class);
    }

    // --- startBreak ---

    @Test
    void startBreak_addsBreak_whenValid() {
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        SessionResponse response = sessionService.startBreak("user-1", "s1");

        assertThat(response.breaks()).hasSize(1);
        verify(sessionRepository).update(any());
    }

    @Test
    void startBreak_throwsConflict_whenSessionEnded() {
        when(sessionRepository.findById("s2")).thenReturn(Optional.of(endedSession));

        assertThatThrownBy(() -> sessionService.startBreak("user-1", "s2"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void startBreak_throwsConflict_whenBreakAlreadyActive() {
        Break activeBreak = Break.builder()
                .id("b1").sessionId("s1")
                .startedAt(LocalDateTime.now())
                .build();
        activeSession.getBreaks().add(activeBreak);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> sessionService.startBreak("user-1", "s1"))
                .isInstanceOf(ConflictException.class);
    }

    // --- endBreak ---

    @Test
    void endBreak_endsBreak_whenValid() {
        Break activeBreak = Break.builder()
                .id("b1").sessionId("s1")
                .startedAt(LocalDateTime.now())
                .build();
        activeSession.getBreaks().add(activeBreak);
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        sessionService.endBreak("user-1", "s1");

        assertThat(activeBreak.getEndedAt()).isNotNull();
        verify(sessionRepository).update(any());
    }

    @Test
    void endBreak_throwsNotFound_whenNoActiveBreak() {
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> sessionService.endBreak("user-1", "s1"))
                .isInstanceOf(NotFoundException.class);
    }

    // --- getSession ---

    @Test
    void getSession_returnsSession_whenOwner() {
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        SessionResponse response = sessionService.getSession("user-1", "s1");

        assertThat(response.id()).isEqualTo("s1");
    }

    @Test
    void getSession_throwsUnauthorized_whenNotOwner() {
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> sessionService.getSession("user-2", "s1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getSession_throwsNotFound_whenMissing() {
        when(sessionRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getSession("user-1", "bad"))
                .isInstanceOf(NotFoundException.class);
    }

    // --- getMySessions ---

    @Test
    void getMySessions_returnsPaginatedSessions() {
        when(sessionRepository.findByUserId("user-1")).thenReturn(List.of(activeSession, endedSession));

        Page<SessionResponse> result = sessionService.getMySessions("user-1", 0, 10);

        assertThat(result.getContent()).hasSize(2);
    }

    // --- getRoomSessions ---

    @Test
    void getRoomSessions_returnsPaginatedSessions() {
        when(sessionRepository.findByRoomId("room-1")).thenReturn(List.of(activeSession));

        Page<SessionResponse> result = sessionService.getRoomSessions("room-1", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    // --- deleteSession ---

    @Test
    void deleteSession_deletesSession_whenOwner() {
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        sessionService.deleteSession("user-1", "s1");

        verify(sessionRepository).delete("s1");
    }

    @Test
    void deleteSession_throwsUnauthorized_whenNotOwner() {
        when(sessionRepository.findById("s1")).thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> sessionService.deleteSession("user-2", "s1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    // --- getMyStats ---

    @Test
    void getMyStats_returnsStats_withEndedSessions() {
        when(sessionRepository.findByUserId("user-1")).thenReturn(List.of(endedSession));

        SessionStatsResponse stats = sessionService.getMyStats("user-1");

        assertThat(stats.totalSessions()).isEqualTo(1);
        assertThat(stats.totalMinutes()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void getMyStats_returnsZeros_whenNoSessions() {
        when(sessionRepository.findByUserId("user-1")).thenReturn(List.of());

        SessionStatsResponse stats = sessionService.getMyStats("user-1");

        assertThat(stats.totalSessions()).isEqualTo(0);
        assertThat(stats.totalMinutes()).isEqualTo(0);
    }

    // --- getRoomStats ---

    @Test
    void getRoomStats_returnsStats() {
        when(sessionRepository.findByRoomId("room-1")).thenReturn(List.of(endedSession));

        SessionStatsResponse stats = sessionService.getRoomStats("room-1");

        assertThat(stats.totalSessions()).isEqualTo(1);
    }

    @Test
    void forceEndActiveSession_endsSession_whenActiveExists() {
        when(sessionRepository.findActiveByUserId("user-1"))
                .thenReturn(Optional.of(activeSession));

        sessionService.forceEndActiveSession("user-1");

        assertThat(activeSession.getEndedAt()).isNotNull();
        assertThat(activeSession.getName()).isEqualTo("Interrupted");
        verify(sessionRepository).update(activeSession);
    }

    @Test
    void forceEndActiveSession_doesNothing_whenNoActiveSession() {
        when(sessionRepository.findActiveByUserId("user-1"))
                .thenReturn(Optional.empty());

        sessionService.forceEndActiveSession("user-1");

        verify(sessionRepository, never()).update(any());
    }

    @Test
    void updateSession_updatesFields_whenValid() {
        when(sessionRepository.findById("s1"))
                .thenReturn(Optional.of(activeSession));

        UpdateSessionRequest request = new UpdateSessionRequest("New Name", "Physics");

        SessionResponse response = sessionService.updateSession("user-1", "s1", request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.subject()).isEqualTo("Physics");
        verify(sessionRepository).update(activeSession);
    }

    @Test
    void updateSession_keepsOldValues_whenNullFields() {
        activeSession.setName("Old");
        activeSession.setSubject("Math");

        when(sessionRepository.findById("s1"))
                .thenReturn(Optional.of(activeSession));

        UpdateSessionRequest request = new UpdateSessionRequest(null, null);

        sessionService.updateSession("user-1", "s1", request);

        assertThat(activeSession.getName()).isEqualTo("Old");
        assertThat(activeSession.getSubject()).isEqualTo("Math");
    }

    @Test
    void updateSession_throwsUnauthorized_whenNotOwner() {
        when(sessionRepository.findById("s1"))
                .thenReturn(Optional.of(activeSession));

        UpdateSessionRequest request = new UpdateSessionRequest("X", "Y");

        assertThatThrownBy(() -> sessionService.updateSession("user-2", "s1", request))
                .isInstanceOf(UnauthorizedException.class);
    }

}