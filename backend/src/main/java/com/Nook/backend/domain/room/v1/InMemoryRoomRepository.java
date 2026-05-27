package com.Nook.backend.domain.room.v1;

import com.Nook.backend.domain.room.IRoomRepository;
import com.Nook.backend.domain.room.Room;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Profile("v1")
@Repository
public class InMemoryRoomRepository implements IRoomRepository {

    private final Map<String, Room> store = new ConcurrentHashMap<>();

    @Override
    public Room save(Room room) {
        store.put(room.getId(), room);
        return room;
    }

    @Override
    public Optional<Room> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Room> findByRoomCode(String roomCode) {
        return store.values().stream()
                .filter(r -> r.getRoomCode().equals(roomCode))
                .findFirst();
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Room> findPublic() {
        return store.values().stream()
                .filter(r -> !r.isPrivate())
                .toList();
    }

    @Override
    public void update(Room room) {
        store.put(room.getId(), room);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public boolean existsByRoomCode(String roomCode) {
        return store.values().stream()
                .anyMatch(r -> r.getRoomCode().equals(roomCode));
    }
}