package com.Nook.backend.domain.chat;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.user.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final IUserRepository userRepository;

    @GetMapping("/room/{roomId}")
    public List<ChatMessage> getRoomMessages(@PathVariable String roomId) {
        return chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request) {
        String username = userRepository.findById(request.userId())
                .map(u -> u.getUsername())
                .orElse("Unknown");

        ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .roomId(request.roomId())
                .userId(request.userId())
                .username(username)
                .content(request.content())
                .sentAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/chat/" + request.roomId(), message);
    }
}