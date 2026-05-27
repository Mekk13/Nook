package com.Nook.backend.domain.chat;

public record ChatMessageRequest(String roomId, String content, String userId) {}