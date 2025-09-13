package com.sarthak.CommunicationService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatSendRequest(
        @NotNull Long conversationId,
        @NotBlank @Size(min = 1, max = 2048) String content
) {}