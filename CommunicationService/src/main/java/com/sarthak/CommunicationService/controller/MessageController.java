package com.sarthak.CommunicationService.controller;

import com.sarthak.CommunicationService.dto.ChatMessageResponse;
import com.sarthak.CommunicationService.dto.ChatSendRequest;
import com.sarthak.CommunicationService.service.ConversationService;
import com.sarthak.CommunicationService.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final ConversationService conversationService;

    public MessageController(MessageService messageService, ConversationService conversationService) {
        this.messageService = messageService;
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ChatSendRequest request
    ){
        // Ensure the user is part of the conversation
        conversationService.mustBelong(request.conversationId(), userId);
        ChatMessageResponse saved = messageService.persist(request.conversationId(), userId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{conversationId}")
    public Page<ChatMessageResponse> getMessages(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Instant before,
            @RequestParam(required = false) Instant after
    ){
        if (before != null && after != null) {
            throw new IllegalArgumentException("Provide only one of 'before' or 'after'");
        }
        // Ensure the user is part of the conversation
        conversationService.mustBelong(conversationId, userId);

        if (after != null) {
            return messageService.getPagedAfter(conversationId, page, size, after);
        }
        return messageService.getPagedBefore(conversationId, page, size, before);
    }
}

