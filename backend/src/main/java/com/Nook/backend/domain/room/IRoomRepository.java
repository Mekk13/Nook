package com.Nook.backend.domain.room;

import java.util.List;
import java.util.Optional;

public interface IRoomRepository {
    Room save(Room room);
    Optional<Room> findById(String id);
    Optional<Room> findByRoomCode(String roomCode);
    List<Room> findAll();
    List<Room> findPublic();
    void update(Room room);
    void delete(String id);
    boolean existsByRoomCode(String roomCode);
}