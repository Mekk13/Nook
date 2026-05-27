package com.Nook.backend.domain.admin;

import com.Nook.backend.domain.log.ActionLog;
import com.Nook.backend.domain.log.ActionLogRepository;
import com.Nook.backend.domain.log.WatchlistEntry;
import com.Nook.backend.domain.log.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final WatchlistRepository watchlistRepository;
    private final ActionLogRepository actionLogRepository;

    @GetMapping("/watchlist")
    public ResponseEntity<List<WatchlistEntry>> getWatchlist() {
        return ResponseEntity.ok(watchlistRepository.findAll());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ActionLog>> getLogs() {
        return ResponseEntity.ok(actionLogRepository.findAll());
    }

    @DeleteMapping("/watchlist/{id}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable String id) {
        watchlistRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}