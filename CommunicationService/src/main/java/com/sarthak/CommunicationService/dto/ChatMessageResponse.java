package com.sarthak.CommunicationService.dto;

import java.time.Instant;

public record ChatMessageResponse(
        Long id,
        Long conversationId,
        String senderId,
        String content,
        Instant createdAt
) {}
