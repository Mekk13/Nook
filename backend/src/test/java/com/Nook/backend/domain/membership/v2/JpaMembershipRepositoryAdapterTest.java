package com.Nook.backend.domain.membership.v2;

import com.Nook.backend.domain.membership.MemberRole;
import com.Nook.backend.domain.membership.MemberStatus;
import com.Nook.backend.domain.membership.RoomMembership;
import com.Nook.backend.domain.membership.IMembershipRepository;
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
class JpaMembershipRepositoryAdapterTest {

    @Autowired
    private IMembershipRepository repository;

    @BeforeEach
    void setUp() {
        repository.findByRoomId("r1")
                .forEach(m -> repository.delete(m.getUserId(), m.getRoomId()));
    }

    private RoomMembership build(String id, String userId, String roomId) {
        return RoomMembership.builder()
                .id(id)
                .userId(userId)
                .roomId(roomId)
                .status(MemberStatus.IDLE)
                .role(MemberRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_and_findByUserIdAndRoomId() {
        repository.save(build("m1", "u1", "r1"));

        Optional<RoomMembership> result =
                repository.findByUserIdAndRoomId("u1", "r1");

        assertThat(result).isPresent();
    }

    @Test
    void findByRoomId() {
        repository.save(build("m1", "u1", "r1"));
        repository.save(build("m2", "u2", "r1"));

        List<RoomMembership> result = repository.findByRoomId("r1");

        assertThat(result).hasSize(2);
    }

    @Test
    void findByUserId() {
        repository.save(build("m1", "u1", "r1"));
        repository.save(build("m2", "u1", "r2"));

        List<RoomMembership> result = repository.findByUserId("u1");

        assertThat(result).hasSize(2);
    }

    @Test
    void update_membership() {
        repository.save(build("m1", "u1", "r1"));

        RoomMembership updated = build("m1", "u1", "r1");
        updated.setStatus(MemberStatus.STUDYING);

        repository.update(updated);

        assertThat(repository.findByUserIdAndRoomId("u1", "r1").get().getStatus())
                .isEqualTo(MemberStatus.STUDYING);
    }

    @Test
    void delete_membership() {
        repository.save(build("m1", "u1", "r1"));

        repository.delete("u1", "r1");

        assertThat(repository.findByUserIdAndRoomId("u1", "r1")).isEmpty();
    }

    @Test
    void existsByUserIdAndRoomId() {
        repository.save(build("m1", "u1", "r1"));

        assertThat(repository.existsByUserIdAndRoomId("u1", "r1")).isTrue();
        assertThat(repository.existsByUserIdAndRoomId("u9", "r9")).isFalse();
    }

    @Test
    void countByRoomId() {
        repository.save(build("m1", "u1", "r1"));
        repository.save(build("m2", "u2", "r1"));

        int count = repository.countByRoomId("r1");

        assertThat(count).isEqualTo(2);
    }
}