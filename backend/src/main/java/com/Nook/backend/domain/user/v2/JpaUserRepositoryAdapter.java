package com.Nook.backend.domain.user.v2;

import com.Nook.backend.domain.user.IUserRepository;
import com.Nook.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Profile("v2")
@Repository
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements IUserRepository {

    private final JpaUserRepository jpa;

    @Override public User save(User user)                          { return jpa.save(user); }
    @Override public Optional<User> findById(String id)            { return jpa.findById(id); }
    @Override public Optional<User> findByEmail(String email)      { return jpa.findByEmailIgnoreCase(email); }
    @Override public Optional<User> findByUsername(String username){ return jpa.findByUsernameIgnoreCase(username); }
    @Override public List<User> findAll()                          { return jpa.findAll(); }
    @Override public void update(User user)                        { jpa.save(user); }
    @Override public void delete(String id)                        { jpa.deleteById(id); }
    @Override public boolean existsByEmail(String email)           { return jpa.existsByEmailIgnoreCase(email); }
    @Override public boolean existsByUsername(String username)     { return jpa.existsByUsernameIgnoreCase(username); }
    @Override public Optional<User> findByResetToken(String token) { return jpa.findByResetToken(token); }
    @Override public Optional<User> findByMagicLinkToken(String t) { return jpa.findByMagicLinkToken(t); }
}