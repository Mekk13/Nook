package com.Nook.backend.domain.room.v1;

import com.Nook.backend.domain.room.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRoomRepositoryTest {

    private InMemoryRoomRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRoomRepository();
    }

    private Room buildRoom(String id, String code, boolean isPrivate) {
        return Room.builder()
                .id(id)
                .name("Test Room")
                .creatorId("user-1")
                .roomCode(code)
                .maxParticipants(10)
                .isPrivate(isPrivate)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_and_findById_returnsRoom() {
        repository.save(buildRoom("r1", "ABC123", false));
        Optional<Room> found = repository.findById("r1");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Room");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        assertThat(repository.findById("nonexistent")).isEmpty();
    }

    @Test
    void findByRoomCode_returnsCorrectRoom() {
        repository.save(buildRoom("r1", "CODE01", false));
        assertThat(repository.findByRoomCode("CODE01")).isPresent();
    }

    @Test
    void findByRoomCode_notFound_returnsEmpty() {
        assertThat(repository.findByRoomCode("NOPE99")).isEmpty();
    }

    @Test
    void findAll_returnsAllRooms() {
        repository.save(buildRoom("r1", "AAA111", false));
        repository.save(buildRoom("r2", "BBB222", true));
        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void findPublic_returnsOnlyPublicRooms() {
        repository.save(buildRoom("r1", "AAA111", false));
        repository.save(buildRoom("r2", "BBB222", true));
        List<Room> publicRooms = repository.findPublic();
        assertThat(publicRooms).hasSize(1);
        assertThat(publicRooms.get(0).getId()).isEqualTo("r1");
    }

    @Test
    void update_replacesExistingRoom() {
        repository.save(buildRoom("r1", "CODE01", false));
        Room updated = buildRoom("r1", "CODE01", false);
        updated.setName("Updated Name");
        repository.update(updated);
        assertThat(repository.findById("r1").get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void delete_removesRoom() {
        repository.save(buildRoom("r1", "CODE01", false));
        repository.delete("r1");
        assertThat(repository.findById("r1")).isEmpty();
    }

    @Test
    void existsByRoomCode_returnsTrue_whenExists() {
        repository.save(buildRoom("r1", "CODE01", false));
        assertThat(repository.existsByRoomCode("CODE01")).isTrue();
    }

    @Test
    void existsByRoomCode_returnsFalse_whenNotExists() {
        assertThat(repository.existsByRoomCode("NOPE99")).isFalse();
    }
}