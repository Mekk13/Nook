package com.Nook.backend.domain.user.v1;

import com.Nook.backend.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    private User buildUser(String id, String email, String username) {
        return User.builder()
                .id(id)
                .fullName("Test User")
                .username(username)
                .email(email)
                .passwordHash("hashed")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_and_findById_returnsUser() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        Optional<User> result = repository.findById("u1");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("a@test.com");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        assertThat(repository.findById("missing")).isEmpty();
    }

    @Test
    void findByEmail_returnsUser_caseInsensitive() {
        repository.save(buildUser("u1", "TEST@MAIL.COM", "alice"));

        Optional<User> result = repository.findByEmail("test@mail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("u1");
    }

    @Test
    void findAll_returnsAllUsers() {
        repository.save(buildUser("u1", "a@test.com", "alice"));
        repository.save(buildUser("u2", "b@test.com", "bob"));

        List<User> users = repository.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void update_replacesExistingUser() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        User updated = buildUser("u1", "a@test.com", "alice2");
        repository.update(updated);

        assertThat(repository.findById("u1").get().getUsername())
                .isEqualTo("alice2");
    }

    @Test
    void delete_removesUser() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        repository.delete("u1");

        assertThat(repository.findById("u1")).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrue_andFalse() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        assertThat(repository.existsByEmail("a@test.com")).isTrue();
        assertThat(repository.existsByEmail("missing@test.com")).isFalse();
    }

    @Test
    void findByUsername_returnsUser_caseInsensitive() {
        repository.save(buildUser("u1", "a@test.com", "Alice"));

        Optional<User> result = repository.findByUsername("alice");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("u1");
    }

    @Test
    void existsByUsername_returnsTrue_andFalse() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        assertThat(repository.existsByUsername("alice")).isTrue();
        assertThat(repository.existsByUsername("bob")).isFalse();
    }
}