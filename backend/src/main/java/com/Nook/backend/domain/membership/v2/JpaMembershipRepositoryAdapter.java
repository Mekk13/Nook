package com.Nook.backend.domain.membership.v2;

import com.Nook.backend.domain.membership.IMembershipRepository;
import com.Nook.backend.domain.membership.RoomMembership;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Profile("v2")
@Repository
@RequiredArgsConstructor
public class JpaMembershipRepositoryAdapter implements IMembershipRepository {

    private final JpaMembershipRepository jpa;

    @Override
    public RoomMembership save(RoomMembership membership) { return jpa.save(membership); }

    @Override
    public Optional<RoomMembership> findByUserIdAndRoomId(String userId, String roomId) {
        return jpa.findByUserIdAndRoomId(userId, roomId);
    }

    @Override
    public List<RoomMembership> findByRoomId(String roomId) { return jpa.findByRoomId(roomId); }

    @Override
    public List<RoomMembership> findByUserId(String userId) { return jpa.findByUserId(userId); }

    @Override
    public void update(RoomMembership membership) { jpa.save(membership); }

    @Override
    @Transactional
    public void delete(String userId, String roomId) {
        jpa.deleteByUserIdAndRoomId(userId, roomId);
    }

    @Override
    public boolean existsByUserIdAndRoomId(String userId, String roomId) {
        return jpa.existsByUserIdAndRoomId(userId, roomId);
    }

    @Override
    public int countByRoomId(String roomId) { return jpa.countByRoomId(roomId); }
}