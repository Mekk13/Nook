package com.Nook.backend.domain.faker;

import com.Nook.backend.domain.membership.*;
import com.Nook.backend.domain.room.IRoomRepository;
import com.Nook.backend.domain.room.Room;
import com.Nook.backend.domain.session.IStudySessionRepository;
import com.Nook.backend.domain.session.StudySession;
import com.Nook.backend.domain.user.IUserRepository;
import com.Nook.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class FakeDataService {
    private String activeRoomId;
    private final IStudySessionRepository sessionRepository;
    private final IRoomRepository roomRepository;
    private final IUserRepository userRepository;
    private final IMembershipRepository membershipRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final Faker faker = new Faker();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;

    public boolean start(String roomId, int intervalSeconds, int batchSize) {
        // If it's already running, stop it first so we can restart with the new roomId
        if (running.get()) {
            stop();
        }

        this.activeRoomId = roomId;
        running.set(true);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        task = scheduler.scheduleAtFixedRate(
                () -> generateSessionBatch(batchSize),
                0, intervalSeconds, TimeUnit.SECONDS
        );
        return true;
    }

    public boolean stop() {
        if (!running.get()) return false;
        running.set(false);
        task.cancel(true);
        scheduler.shutdown();
        return true;
    }

    public boolean isRunning() {
        return running.get();
    }

    private void generateSessionBatch(int batchSize) {
        Room room = roomRepository.findById(activeRoomId).orElse(null);
        if (room == null) return;

        int currentCount = membershipRepository.countByRoomId(activeRoomId);
        int spotsLeft = room.getMaxParticipants() - currentCount;
        int actualBatchSize = Math.min(batchSize, spotsLeft);

        List<String> sessionIds = new ArrayList<>();

        if (actualBatchSize > 0) {
            // Room has space — create new users + sessions
            for (int i = 0; i < actualBatchSize; i++) {
                User user = User.builder()
                        .id(UUID.randomUUID().toString())
                        .fullName(faker.name().fullName())
                        .username(faker.internet().username())
                        .email(faker.internet().emailAddress())
                        .passwordHash("fake-hash")
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(user);

                membershipRepository.save(RoomMembership.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(user.getId())
                        .roomId(activeRoomId)
                        .role(MemberRole.MEMBER)
                        .status(MemberStatus.STUDYING)
                        .joinedAt(LocalDateTime.now())
                        .build());

                StudySession session = StudySession.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(user.getId())
                        .roomId(activeRoomId)
                        .name(user.getFullName())
                        .subject(faker.educator().course())
                        .startedAt(LocalDateTime.now().minusMinutes(faker.number().numberBetween(30, 120)))
                        .endedAt(LocalDateTime.now())
                        .breaks(new ArrayList<>())
                        .build();

                sessionRepository.save(session);
                sessionIds.add(session.getId());
            }
        } else {
            // Room is full — create sessions for existing members
            List<RoomMembership> members = membershipRepository.findByRoomId(activeRoomId);
            for (int i = 0; i < batchSize; i++) {
                RoomMembership randomMember = members.get(faker.number().numberBetween(0, members.size()));

                StudySession session = StudySession.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(randomMember.getUserId())
                        .roomId(activeRoomId)
                        .name(faker.educator().course())
                        .subject(faker.educator().course())
                        .startedAt(LocalDateTime.now().minusMinutes(faker.number().numberBetween(30, 120)))
                        .endedAt(LocalDateTime.now())
                        .breaks(new ArrayList<>())
                        .build();

                sessionRepository.save(session);
                sessionIds.add(session.getId());
            }
        }

        if (!sessionIds.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/room/" + activeRoomId, new FakeDataEvent(
                    "SESSION_BATCH_CREATED",
                    sessionIds.size(),
                    sessionIds
            ));
        }
    }

}