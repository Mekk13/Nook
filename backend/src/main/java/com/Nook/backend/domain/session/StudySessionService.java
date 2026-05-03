package com.Nook.backend.domain.session;

import com.Nook.backend.domain.membership.IMembershipRepository;
import com.Nook.backend.domain.session.dto.*;
import com.Nook.backend.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final IStudySessionRepository sessionRepository;
    private final IMembershipRepository membershipRepository;

    public SessionResponse startSession(String userId, StartSessionRequest request) {
        // Must be a member of the room
        if (!membershipRepository.existsByUserIdAndRoomId(userId, request.roomId())) {
            throw new UnauthorizedException("You are not a member of this room");
        }

        // Cannot have two active sessions
        if (sessionRepository.findActiveByUserId(userId).isPresent()) {
            throw new ConflictException("You already have an active session — end it before starting a new one");
        }

        StudySession session = StudySession.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .roomId(request.roomId())
                .startedAt(LocalDateTime.now())
                .breaks(new java.util.ArrayList<>())
                .build();

        sessionRepository.save(session);
        return SessionResponse.from(session);
    }

    public SessionResponse endSession(String userId, String sessionId, EndSessionRequest request) {
        StudySession session = findSessionOrThrow(sessionId);
        assertOwnsSession(userId, session);

        if (session.getEndedAt() != null) {
            throw new ConflictException("Session is already ended");
        }

        // End any open break first
        session.getBreaks().stream()
                .filter(b -> b.getEndedAt() == null)
                .forEach(b -> b.setEndedAt(LocalDateTime.now()));

        session.setName(request.name());
        session.setSubject(request.subject());
        session.setEndedAt(LocalDateTime.now());

        sessionRepository.update(session);
        return SessionResponse.from(session);
    }

    public SessionResponse startBreak(String userId, String sessionId) {
        StudySession session = findSessionOrThrow(sessionId);
        assertOwnsSession(userId, session);

        if (session.getEndedAt() != null) {
            throw new ConflictException("Cannot start a break on an ended session");
        }

        boolean breakAlreadyActive = session.getBreaks().stream()
                .anyMatch(b -> b.getEndedAt() == null);
        if (breakAlreadyActive) {
            throw new ConflictException("You already have an active break");
        }

        Break b = Break.builder()
                .id(UUID.randomUUID().toString())
                .sessionId(sessionId)
                .startedAt(LocalDateTime.now())
                .build();

        session.getBreaks().add(b);
        sessionRepository.update(session);
        return SessionResponse.from(session);
    }

    public SessionResponse endBreak(String userId, String sessionId) {
        StudySession session = findSessionOrThrow(sessionId);
        assertOwnsSession(userId, session);

        Break activeBreak = session.getBreaks().stream()
                .filter(b -> b.getEndedAt() == null)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No active break found"));

        activeBreak.setEndedAt(LocalDateTime.now());
        sessionRepository.update(session);
        return SessionResponse.from(session);
    }

    public Page<SessionResponse> getMySessions(String userId, int page, int size) {
        List<StudySession> all = sessionRepository.findByUserId(userId);
        return paginate(all, page, size);
    }

    public SessionResponse getSession(String userId, String sessionId) {
        StudySession session = findSessionOrThrow(sessionId);
        assertOwnsSession(userId, session);
        return SessionResponse.from(session);
    }

    public Page<SessionResponse> getRoomSessions(String roomId, int page, int size) {
        List<StudySession> all = sessionRepository.findByRoomId(roomId);
        return paginate(all, page, size);
    }

    public SessionStatsResponse getMyStats(String userId) {
        List<StudySession> sessions = sessionRepository.findByUserId(userId)
                .stream()
                .filter(s -> s.getEndedAt() != null)
                .toList();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = startOfDay.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        return new SessionStatsResponse(
                sumMinutes(sessions),
                sumMinutes(sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfDay)).toList()),
                sumMinutes(sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfWeek)).toList()),
                sumMinutes(sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfMonth)).toList()),
                sessions.size(),
                (int) sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfDay)).count(),
                (int) sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfWeek)).count(),
                (int) sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfMonth)).count()
        );
    }

    public SessionStatsResponse getRoomStats(String roomId) {
        List<StudySession> sessions = sessionRepository.findByRoomId(roomId)
                .stream()
                .filter(s -> s.getEndedAt() != null)
                .toList();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = startOfDay.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        return new SessionStatsResponse(
                sumMinutes(sessions),
                sumMinutes(sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfDay)).toList()),
                sumMinutes(sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfWeek)).toList()),
                sumMinutes(sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfMonth)).toList()),
                sessions.size(),
                (int) sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfDay)).count(),
                (int) sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfWeek)).count(),
                (int) sessions.stream().filter(s -> s.getStartedAt().isAfter(startOfMonth)).count()
        );
    }

    public void deleteSession(String userId, String sessionId) {
        StudySession session = findSessionOrThrow(sessionId);
        assertOwnsSession(userId, session);
        sessionRepository.delete(sessionId);
    }


    private StudySession findSessionOrThrow(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
    }

    private void assertOwnsSession(String userId, StudySession session) {
        if (!session.getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not own this session");
        }
    }

    private long sumMinutes(List<StudySession> sessions) {
        return sessions.stream()
                .mapToLong(s -> ChronoUnit.MINUTES.between(s.getStartedAt(), s.getEndedAt()))
                .sum();
    }

    private Page<SessionResponse> paginate(List<StudySession> sessions, int page, int size) {
        // Sort newest-first so pagination is stable and predictable
        List<StudySession> sorted = sessions.stream()
                .sorted(Comparator.comparing(StudySession::getStartedAt))
                .toList();

        int total = sorted.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<SessionResponse> content = sorted.subList(from, to).stream()
                .map(SessionResponse::from)
                .toList();
        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }

    public void forceEndActiveSession(String userId) {
        sessionRepository.findActiveByUserId(userId).ifPresent(session -> {
            session.getBreaks().stream()
                    .filter(b -> b.getEndedAt() == null)
                    .forEach(b -> b.setEndedAt(LocalDateTime.now()));
            session.setEndedAt(LocalDateTime.now());
            session.setName("Interrupted");
            session.setSubject("Unknown");
            sessionRepository.update(session);
        });
    }

    public SessionResponse updateSession(String userId, String sessionId, UpdateSessionRequest request) {
        StudySession session = findSessionOrThrow(sessionId);
        assertOwnsSession(userId, session);
        if (request.name() != null) session.setName(request.name());
        if (request.subject() != null) session.setSubject(request.subject());
        sessionRepository.update(session);
        return SessionResponse.from(session);
    }
}