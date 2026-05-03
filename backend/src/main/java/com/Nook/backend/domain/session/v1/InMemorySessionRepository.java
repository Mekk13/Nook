package com.Nook.backend.domain.session.v1;

import com.Nook.backend.domain.session.IStudySessionRepository;
import com.Nook.backend.domain.session.StudySession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Profile("v1")
@Repository
public class InMemorySessionRepository implements IStudySessionRepository {

    private final Map<String, StudySession> store = new ConcurrentHashMap<>();

    @Override
    public StudySession save(StudySession session) {
        store.put(session.getId(), session);
        return session;
    }

    @Override
    public Optional<StudySession> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<StudySession> findByUserId(String userId) {
        return store.values().stream()
                .filter(s -> s.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<StudySession> findByRoomId(String roomId) {
        return store.values().stream()
                .filter(s -> s.getRoomId().equals(roomId))
                .toList();
    }

    @Override
    public Optional<StudySession> findActiveByUserId(String userId) {
        // Active = session exists for this user AND endedAt is null
        return store.values().stream()
                .filter(s -> s.getUserId().equals(userId) && s.getEndedAt() == null)
                .findFirst();
    }

    @Override
    public void update(StudySession session) {
        store.put(session.getId(), session);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }
}