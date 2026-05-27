package com.Nook.backend.domain.log;

import com.Nook.backend.domain.user.IUserRepository;
import com.Nook.backend.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoggingService {

    private final ActionLogRepository actionLogRepository;
    private final WatchlistRepository watchlistRepository;
    private final IUserRepository userRepository;

    private static final int ROOM_CREATE_LIMIT = 5;
    private static final int ROOM_CREATE_WINDOW_MINUTES = 2;

    public void log(String userId, String action) {
        UserGroup group = userRepository.findById(userId)
                .map(u -> u.getRole() == UserRole.ADMIN ? UserGroup.ADMIN : UserGroup.USER)
                .orElse(UserGroup.USER);

        ActionLog entry = ActionLog.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .group(group)
                .action(action)
                .timestamp(LocalDateTime.now())
                .build();

        actionLogRepository.save(entry);
        checkForMaliciousBehaviour(userId, action);
    }

    private void checkForMaliciousBehaviour(String userId, String action) {
        if (!action.equals("CREATE_ROOM")) return;
        if (watchlistRepository.existsByUserId(userId)) return;

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(ROOM_CREATE_WINDOW_MINUTES);
        List<ActionLog> recent = actionLogRepository
                .findByUserIdAndActionAndTimestampAfter(userId, "CREATE_ROOM", windowStart);

        if (recent.size() >= ROOM_CREATE_LIMIT) {
            String username = userRepository.findById(userId)
                    .map(u -> u.getUsername())
                    .orElse("Unknown");

            WatchlistEntry watchlistEntry = WatchlistEntry.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .username(username)
                    .reason("Created " + recent.size() + " rooms within 2 minutes")
                    .flaggedAt(LocalDateTime.now())
                    .build();

            watchlistRepository.save(watchlistEntry);
        }
    }
}