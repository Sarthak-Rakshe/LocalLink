package com.sarthak.CommunicationService.dto;

import java.time.Instant;

public record ConversationResponse(
        Long id,
        String otherUserId,
        String LastMessageSnippet,
        Instant updatedAt
) {}
