package com.Nook.backend.domain.membership.v1;

import com.Nook.backend.domain.membership.IMembershipRepository;
import com.Nook.backend.domain.membership.RoomMembership;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Profile("v1")
@Repository
public class InMemoryMembershipRepository implements IMembershipRepository {

    // Key: membershipId, Value: RoomMembership
    private final Map<String, RoomMembership> store = new ConcurrentHashMap<>();

    @Override
    public RoomMembership save(RoomMembership membership) {
        store.put(membership.getId(), membership);
        return membership;
    }

    @Override
    public Optional<RoomMembership> findByUserIdAndRoomId(String userId, String roomId) {
        return store.values().stream()
                .filter(m -> m.getUserId().equals(userId) && m.getRoomId().equals(roomId))
                .findFirst();
    }

    @Override
    public List<RoomMembership> findByRoomId(String roomId) {
        return store.values().stream()
                .filter(m -> m.getRoomId().equals(roomId))
                .toList();
    }

    @Override
    public List<RoomMembership> findByUserId(String userId) {
        return store.values().stream()
                .filter(m -> m.getUserId().equals(userId))
                .toList();
    }

    @Override
    public void update(RoomMembership membership) {
        store.put(membership.getId(), membership);
    }

    @Override
    public void delete(String userId, String roomId) {
        store.values().stream()
                .filter(m -> m.getUserId().equals(userId) && m.getRoomId().equals(roomId))
                .findFirst()
                .ifPresent(m -> store.remove(m.getId()));
    }

    @Override
    public boolean existsByUserIdAndRoomId(String userId, String roomId) {
        return store.values().stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.getRoomId().equals(roomId));
    }

    @Override
    public int countByRoomId(String roomId) {
        return (int) store.values().stream()
                .filter(m -> m.getRoomId().equals(roomId))
                .count();
    }
}