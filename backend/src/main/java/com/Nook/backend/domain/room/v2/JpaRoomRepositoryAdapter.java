package com.Nook.backend.domain.room.v2;

import com.Nook.backend.domain.room.IRoomRepository;
import com.Nook.backend.domain.room.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Profile("v2")
@Repository
@RequiredArgsConstructor
public class JpaRoomRepositoryAdapter implements IRoomRepository {

    private final JpaRoomRepository jpa;

    @Override
    public Room save(Room room) {
        return jpa.save(room);
    }

    @Override
    public Optional<Room> findById(String id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Room> findByRoomCode(String roomCode) {
        return jpa.findByRoomCode(roomCode);
    }

    @Override
    public List<Room> findAll() {
        return jpa.findAll();
    }

    @Override
    public List<Room> findPublic() {
        return jpa.findByIsPrivateFalse();
    }

    @Override
    public void update(Room room) {
        jpa.save(room);
    }

    @Override
    public void delete(String id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsByRoomCode(String roomCode) {
        return jpa.existsByRoomCode(roomCode);
    }
}