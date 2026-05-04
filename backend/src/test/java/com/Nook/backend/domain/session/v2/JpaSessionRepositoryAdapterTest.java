package com.Nook.backend.domain.session.v2;

import com.Nook.backend.domain.session.Break;
import com.Nook.backend.domain.session.IStudySessionRepository;
import com.Nook.backend.domain.session.StudySession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JpaSessionRepositoryAdapterTest {

    @Autowired
    private IStudySessionRepository repository;

    @BeforeEach
    void setUp() {
        repository.findByUserId("u1").forEach(s -> repository.delete(s.getId()));
    }

    private StudySession buildSession(String id, String userId, String roomId, String subject) {
        return StudySession.builder()
                .id(id)
                .userId(userId)
                .roomId(roomId)
                .name("Session")
                .subject(subject)
                .startedAt(LocalDateTime.now())
                .endedAt(null)
                .breaks(List.of())
                .build();
    }

    @Test
    void save_and_findById() {
        repository.save(buildSession("s1", "u1", "r1", "math"));

        Optional<StudySession> result = repository.findById("s1");

        assertThat(result).isPresent();
        assertThat(result.get().getSubject()).isEqualTo("math");
    }

    @Test
    void findByUserId() {
        repository.save(buildSession("s1", "u1", "r1", "math"));
        repository.save(buildSession("s2", "u1", "r2", "physics"));

        List<StudySession> result = repository.findByUserId("u1");

        assertThat(result).hasSize(2);
    }

    @Test
    void findByRoomId() {
        repository.save(buildSession("s1", "u1", "roomA", "math"));

        List<StudySession> result = repository.findByRoomId("roomA");

        assertThat(result).hasSize(1);
    }

    @Test
    void findActiveSession() {
        repository.save(buildSession("s1", "u1", "r1", "math"));

        Optional<StudySession> result = repository.findActiveByUserId("u1");

        assertThat(result).isPresent();
    }

    @Test
    void update_session() {
        repository.save(buildSession("s1", "u1", "r1", "math"));

        StudySession updated = buildSession("s1", "u1", "r1", "biology");
        repository.update(updated);

        assertThat(repository.findById("s1").get().getSubject())
                .isEqualTo("biology");
    }

    @Test
    void delete_session() {
        repository.save(buildSession("s1", "u1", "r1", "math"));

        repository.delete("s1");

        assertThat(repository.findById("s1")).isEmpty();
    }

    @Test
    void findByUserIdAndSubjectContaining() {
        repository.save(buildSession("s1", "u1", "r1", "Mathematics"));
        repository.save(buildSession("s2", "u1", "r1", "Physics"));

        List<StudySession> result =
                repository.findByUserIdAndSubjectContainingIgnoreCase("u1", "math");

        assertThat(result).hasSize(1);
    }

    @Test
    void findDistinctSubjects() {
        repository.save(buildSession("s1", "u1", "r1", "Math"));
        repository.save(buildSession("s2", "u1", "r1", "Physics"));
        repository.save(buildSession("s3", "u1", "r1", "Math"));

        List<String> subjects = repository.findDistinctSubjectsByUserId("u1");

        assertThat(subjects).contains("Math", "Physics");
    }
}