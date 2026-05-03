package com.Nook.backend.domain.membership.v1;

import com.Nook.backend.domain.membership.MemberRole;
import com.Nook.backend.domain.membership.MemberStatus;
import com.Nook.backend.domain.membership.RoomMembership;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class InMemoryMembershipRepositoryTest {

    private InMemoryMembershipRepository repo;

    private RoomMembership membership(String id, String userId, String roomId) {
        return RoomMembership.builder()
                .id(id)
                .userId(userId)
                .roomId(roomId)
                .role(MemberRole.MEMBER)
                .status(MemberStatus.IDLE)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setUp() {
        repo = new InMemoryMembershipRepository();
    }

    @Test
    void save_persistsMembership() {
        RoomMembership m = membership("m1", "u1", "r1");

        RoomMembership saved = repo.save(m);

        assertThat(saved).isSameAs(m);
        assertThat(repo.findByUserIdAndRoomId("u1", "r1")).isPresent();
    }

    @Test
    void findByUserIdAndRoomId_found() {
        repo.save(membership("m1", "u1", "r1"));

        Optional<RoomMembership> result = repo.findByUserIdAndRoomId("u1", "r1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("m1");
    }

    @Test
    void findByUserIdAndRoomId_notFound_returnsEmpty() {
        assertThat(repo.findByUserIdAndRoomId("u1", "r1")).isEmpty();
    }

    @Test
    void findByUserIdAndRoomId_wrongRoom_returnsEmpty() {
        repo.save(membership("m1", "u1", "r1"));

        assertThat(repo.findByUserIdAndRoomId("u1", "r2")).isEmpty();
    }

    @Test
    void findByRoomId_returnsOnlyMatchingRoom() {
        repo.save(membership("m1", "u1", "r1"));
        repo.save(membership("m2", "u2", "r1"));
        repo.save(membership("m3", "u3", "r2"));

        List<RoomMembership> result = repo.findByRoomId("r1");

        assertThat(result).hasSize(2)
                .extracting(RoomMembership::getUserId)
                .containsExactlyInAnyOrder("u1", "u2");
    }

    @Test
    void findByRoomId_noMembers_returnsEmpty() {
        assertThat(repo.findByRoomId("r1")).isEmpty();
    }

    @Test
    void findByUserId_returnsAllRoomsForUser() {
        repo.save(membership("m1", "u1", "r1"));
        repo.save(membership("m2", "u1", "r2"));
        repo.save(membership("m3", "u2", "r1"));

        List<RoomMembership> result = repo.findByUserId("u1");

        assertThat(result).hasSize(2)
                .extracting(RoomMembership::getRoomId)
                .containsExactlyInAnyOrder("r1", "r2");
    }

    @Test
    void update_overwritesExistingEntry() {
        RoomMembership m = membership("m1", "u1", "r1");
        repo.save(m);

        m.setRole(MemberRole.MODERATOR);
        repo.update(m);

        assertThat(repo.findByUserIdAndRoomId("u1", "r1").get().getRole())
                .isEqualTo(MemberRole.MODERATOR);
    }

    @Test
    void delete_removesMembership() {
        repo.save(membership("m1", "u1", "r1"));

        repo.delete("u1", "r1");

        assertThat(repo.findByUserIdAndRoomId("u1", "r1")).isEmpty();
    }

    @Test
    void delete_onlyRemovesTargetMembership() {
        repo.save(membership("m1", "u1", "r1"));
        repo.save(membership("m2", "u2", "r1"));

        repo.delete("u1", "r1");

        assertThat(repo.findByUserIdAndRoomId("u2", "r1")).isPresent();
    }

    @Test
    void delete_nonExistent_doesNotThrow() {
        assertThatCode(() -> repo.delete("u1", "r1")).doesNotThrowAnyException();
    }

    @Test
    void existsByUserIdAndRoomId_trueWhenPresent() {
        repo.save(membership("m1", "u1", "r1"));

        assertThat(repo.existsByUserIdAndRoomId("u1", "r1")).isTrue();
    }

    @Test
    void existsByUserIdAndRoomId_falseWhenAbsent() {
        assertThat(repo.existsByUserIdAndRoomId("u1", "r1")).isFalse();
    }

    @Test
    void countByRoomId_returnsCorrectCount() {
        repo.save(membership("m1", "u1", "r1"));
        repo.save(membership("m2", "u2", "r1"));
        repo.save(membership("m3", "u3", "r2"));

        assertThat(repo.countByRoomId("r1")).isEqualTo(2);
    }

    @Test
    void countByRoomId_emptyRoom_returnsZero() {
        assertThat(repo.countByRoomId("r1")).isZero();
    }
}