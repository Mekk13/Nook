package com.Nook.backend.domain.session.v2;

import com.Nook.backend.domain.session.IStudySessionRepository;
import com.Nook.backend.domain.session.StudySession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Profile("v2")
@Repository
@RequiredArgsConstructor
public class JpaSessionRepositoryAdapter implements IStudySessionRepository {

    private final JpaSessionRepository jpa;

    @Override
    public StudySession save(StudySession session) { return jpa.save(session); }

    @Override
    public Optional<StudySession> findById(String id) { return jpa.findById(id); }

    @Override
    public List<StudySession> findByUserId(String userId) { return jpa.findByUserId(userId); }

    @Override
    public List<StudySession> findByRoomId(String roomId) { return jpa.findByRoomId(roomId); }

    @Override
    public Optional<StudySession> findActiveByUserId(String userId) {
        return jpa.findByUserIdAndEndedAtIsNull(userId);
    }

    @Override
    public void update(StudySession session) { jpa.save(session); }

    @Override
    public void delete(String id) { jpa.deleteById(id); }

    @Override
    public List<StudySession> findByUserIdAndSubjectContainingIgnoreCase(String userId, String subject) {
        return jpa.findByUserIdAndSubjectContainingIgnoreCase(userId, subject);
    }

    @Override
    public List<String> findDistinctSubjectsByUserId(String userId) {
        return jpa.findDistinctSubjectsByUserId(userId);
    }
}