package com.Nook.backend.domain.user;

import java.util.List;
import java.util.Optional;

public interface IUserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void update(User user);
    void delete(String id);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByResetToken(String token);
    Optional<User> findByMagicLinkToken(String token);
}