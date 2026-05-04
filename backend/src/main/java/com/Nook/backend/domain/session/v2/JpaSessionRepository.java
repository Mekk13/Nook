package com.Nook.backend.domain.session.v2;

import com.Nook.backend.domain.session.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaSessionRepository extends JpaRepository<StudySession, String> {
    List<StudySession> findByUserId(String userId);
    List<StudySession> findByRoomId(String roomId);
    Optional<StudySession> findByUserIdAndEndedAtIsNull(String userId);
    List<StudySession> findByUserIdAndSubjectContainingIgnoreCase(String userId, String subject);
    @Query("SELECT DISTINCT s.subject FROM StudySession s WHERE s.userId = :userId AND s.subject IS NOT NULL")
    List<String> findDistinctSubjectsByUserId(@Param("userId") String userId);
}