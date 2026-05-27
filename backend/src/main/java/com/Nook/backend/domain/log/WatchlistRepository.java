package com.Nook.backend.domain.log;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistRepository extends JpaRepository<WatchlistEntry, String> {
    boolean existsByUserId(String userId);
}