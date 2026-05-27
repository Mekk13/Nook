package com.Nook.backend.domain.session;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "study_sessions")
public class StudySession {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "room_id")
    private String roomId;

    private String name;
    private String subject;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    @Builder.Default
    private List<Break> breaks = new ArrayList<>();
}