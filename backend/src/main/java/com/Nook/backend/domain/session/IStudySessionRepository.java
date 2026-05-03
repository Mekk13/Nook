package com.Nook.backend.domain.session;

import java.util.List;
import java.util.Optional;

public interface IStudySessionRepository {
    StudySession save(StudySession session);
    Optional<StudySession> findById(String id);

    List<StudySession> findByUserId(String userId);

    List<StudySession> findByRoomId(String roomId);

    Optional<StudySession> findActiveByUserId(String userId);

    void update(StudySession session);
    void delete(String id);
}