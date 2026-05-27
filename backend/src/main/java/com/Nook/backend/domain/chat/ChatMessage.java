package com.Nook.backend.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class ChatMessage {

    @Id
    private String id;
    private String roomId;
    private String userId;
    private String username;
    private String content;
    private LocalDateTime sentAt;
}