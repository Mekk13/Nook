package com.Nook.backend.domain.session;

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
@Table(name = "breaks")
public class Break {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(name = "session_id", nullable = false, insertable = false, updatable = false)
    private String sessionId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
}