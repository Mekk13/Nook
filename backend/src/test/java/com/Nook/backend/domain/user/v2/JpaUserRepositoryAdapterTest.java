package com.Nook.backend.domain.user.v2;

import com.Nook.backend.domain.user.User;
import com.Nook.backend.domain.user.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JpaUserRepositoryAdapterTest {

    @Autowired
    private IUserRepository repository;

    @BeforeEach
    void setUp() {
        repository.findAll().forEach(u -> repository.delete(u.getId()));
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
    void save_and_findById() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        Optional<User> result = repository.findById("u1");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("a@test.com");
    }

    @Test
    void findByEmail() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        assertThat(repository.findByEmail("a@test.com")).isPresent();
    }

    @Test
    void findByUsername() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        assertThat(repository.findByUsername("alice")).isPresent();
    }

    @Test
    void update_user() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        User updated = buildUser("u1", "a@test.com", "alice2");
        repository.update(updated);

        assertThat(repository.findById("u1").get().getUsername())
                .isEqualTo("alice2");
    }

    @Test
    void delete_user() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        repository.delete("u1");

        assertThat(repository.findById("u1")).isEmpty();
    }

    @Test
    void exists_checks() {
        repository.save(buildUser("u1", "a@test.com", "alice"));

        assertThat(repository.existsByEmail("a@test.com")).isTrue();
        assertThat(repository.existsByUsername("alice")).isTrue();
    }
}