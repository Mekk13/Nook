package com.Nook.backend.domain.session.v1;

import com.Nook.backend.domain.session.StudySession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemorySessionRepositoryTest {

    private InMemorySessionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySessionRepository();
    }

    private StudySession buildSession(String id, String userId, String roomId, boolean ended) {
        return StudySession.builder()
                .id(id)
                .userId(userId)
                .roomId(roomId)
                .startedAt(LocalDateTime.now())
                .endedAt(ended ? LocalDateTime.now() : null)
                .build();
    }

    @Test
    void save_and_findById_returnsSession() {
        repository.save(buildSession("s1", "user-1", "room-1", false));
        Optional<StudySession> found = repository.findById("s1");
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("user-1");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        assertThat(repository.findById("nonexistent")).isEmpty();
    }

    @Test
    void findByUserId_returnsOnlyUserSessions() {
        repository.save(buildSession("s1", "user-1", "room-1", false));
        repository.save(buildSession("s2", "user-2", "room-1", false));
        List<StudySession> result = repository.findByUserId("user-1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("s1");
    }

    @Test
    void findByUserId_returnsEmpty_whenNoSessions() {
        assertThat(repository.findByUserId("user-99")).isEmpty();
    }

    @Test
    void findByRoomId_returnsOnlyRoomSessions() {
        repository.save(buildSession("s1", "user-1", "room-1", false));
        repository.save(buildSession("s2", "user-1", "room-2", false));
        List<StudySession> result = repository.findByRoomId("room-1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("s1");
    }

    @Test
    void findByRoomId_returnsEmpty_whenNoSessions() {
        assertThat(repository.findByRoomId("room-99")).isEmpty();
    }

    @Test
    void findActiveByUserId_returnsActiveSession() {
        repository.save(buildSession("s1", "user-1", "room-1", false));
        Optional<StudySession> active = repository.findActiveByUserId("user-1");
        assertThat(active).isPresent();
        assertThat(active.get().getId()).isEqualTo("s1");
    }

    @Test
    void findActiveByUserId_returnsEmpty_whenAllEnded() {
        repository.save(buildSession("s1", "user-1", "room-1", true));
        assertThat(repository.findActiveByUserId("user-1")).isEmpty();
    }

    @Test
    void findActiveByUserId_returnsEmpty_whenNoSessions() {
        assertThat(repository.findActiveByUserId("user-99")).isEmpty();
    }

    @Test
    void update_replacesSession() {
        repository.save(buildSession("s1", "user-1", "room-1", false));
        StudySession updated = buildSession("s1", "user-1", "room-1", true);
        repository.update(updated);
        assertThat(repository.findById("s1").get().getEndedAt()).isNotNull();
    }

    @Test
    void delete_removesSession() {
        repository.save(buildSession("s1", "user-1", "room-1", false));
        repository.delete("s1");
        assertThat(repository.findById("s1")).isEmpty();
    }
}