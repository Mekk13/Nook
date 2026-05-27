package com.Nook.backend.domain.room.v2;

import com.Nook.backend.domain.room.IRoomRepository;
import com.Nook.backend.domain.room.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JpaRoomRepositoryAdapterTest {

    @Autowired
    private IRoomRepository repository;

    @BeforeEach
    void setUp() {
        repository.findAll().forEach(r -> repository.delete(r.getId()));
    }

    private Room buildRoom(String id, String code, boolean isPrivate) {
        return Room.builder()
                .id(id)
                .name("Room")
                .creatorId("u1")
                .roomCode(code)
                .description("desc")
                .maxParticipants(10)
                .isPrivate(isPrivate)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_and_findById() {
        repository.save(buildRoom("r1", "ABC123", false));

        Optional<Room> result = repository.findById("r1");

        assertThat(result).isPresent();
        assertThat(result.get().getRoomCode()).isEqualTo("ABC123");
    }

    @Test
    void findByRoomCode() {
        repository.save(buildRoom("r1", "ABC123", false));

        assertThat(repository.findByRoomCode("ABC123")).isPresent();
    }

    @Test
    void findAll_returnsRooms() {
        repository.save(buildRoom("r1", "ABC123", false));
        repository.save(buildRoom("r2", "XYZ999", true));

        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void findPublic_returnsOnlyPublicRooms() {
        repository.save(buildRoom("r1", "ABC123", false));
        repository.save(buildRoom("r2", "XYZ999", true));

        List<Room> result = repository.findPublic();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isPrivate()).isFalse();
    }

    @Test
    void update_room() {
        repository.save(buildRoom("r1", "ABC123", false));

        Room updated = buildRoom("r1", "ABC123", true);
        repository.update(updated);

        assertThat(repository.findById("r1").get().isPrivate()).isTrue();
    }

    @Test
    void delete_room() {
        repository.save(buildRoom("r1", "ABC123", false));

        repository.delete("r1");

        assertThat(repository.findById("r1")).isEmpty();
    }

    @Test
    void existsByRoomCode() {
        repository.save(buildRoom("r1", "ABC123", false));

        assertThat(repository.existsByRoomCode("ABC123")).isTrue();
        assertThat(repository.existsByRoomCode("NOPE99")).isFalse();
    }
}