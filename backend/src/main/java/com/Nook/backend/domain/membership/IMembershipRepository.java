package com.Nook.backend.domain.membership;

import java.util.List;
import java.util.Optional;

public interface IMembershipRepository {
    RoomMembership save(RoomMembership membership);

    Optional<RoomMembership> findByUserIdAndRoomId(String userId, String roomId);

    List<RoomMembership> findByRoomId(String roomId);

    List<RoomMembership> findByUserId(String userId);

    void update(RoomMembership membership);
    void delete(String userId, String roomId);

    boolean existsByUserIdAndRoomId(String userId, String roomId);

    int countByRoomId(String roomId);
}