package com.Nook.backend.domain.membership.v2;

import com.Nook.backend.domain.membership.RoomMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaMembershipRepository extends JpaRepository<RoomMembership, String> {
    Optional<RoomMembership> findByUserIdAndRoomId(String userId, String roomId);
    List<RoomMembership> findByRoomId(String roomId);
    List<RoomMembership> findByUserId(String userId);
    boolean existsByUserIdAndRoomId(String userId, String roomId);
    int countByRoomId(String roomId);
    void deleteByUserIdAndRoomId(String userId, String roomId);
}