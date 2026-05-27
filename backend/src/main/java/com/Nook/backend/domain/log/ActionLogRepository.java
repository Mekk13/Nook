package com.Nook.backend.domain.log;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ActionLogRepository extends JpaRepository<ActionLog, String> {
    List<ActionLog> findByUserIdAndActionAndTimestampAfter(
            String userId, String action, LocalDateTime after
    );
}