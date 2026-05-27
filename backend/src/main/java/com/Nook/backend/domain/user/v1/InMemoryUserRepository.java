package com.Nook.backend.domain.user.v1;

import com.Nook.backend.domain.user.IUserRepository;
import com.Nook.backend.domain.user.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Profile("v1")
@Repository
public class InMemoryUserRepository implements IUserRepository {

    // Key: userId, Value: the User object
    private final Map<String, User> store = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return store.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void update(User user) {
        store.put(user.getId(), user);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return store.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        return store.values().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    @Override
    public Optional<User> findByResetToken(String token) {
        if (token == null) return Optional.empty();

        return store.values().stream()
                .filter(u -> token.equals(u.getResetToken()))
                .findFirst();
    }

    @Override
    public Optional<User> findByMagicLinkToken(String token) {
        return Optional.empty();
    }
}