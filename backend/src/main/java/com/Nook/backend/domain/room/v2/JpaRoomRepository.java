package com.Nook.backend.domain.room.v2;

import com.Nook.backend.domain.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaRoomRepository extends JpaRepository<Room, String> {
    Optional<Room> findByRoomCode(String roomCode);
    List<Room> findByIsPrivateFalse();
    boolean existsByRoomCode(String roomCode);
}