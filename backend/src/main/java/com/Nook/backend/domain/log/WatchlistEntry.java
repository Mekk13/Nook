package com.Nook.backend.domain.log;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "watchlist")
public class WatchlistEntry {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String reason;

    @Column(name = "flagged_at", nullable = false)
    private LocalDateTime flaggedAt;
}