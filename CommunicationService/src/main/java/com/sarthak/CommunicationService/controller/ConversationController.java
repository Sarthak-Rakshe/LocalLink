package com.sarthak.CommunicationService.controller;

import com.sarthak.CommunicationService.dto.ConversationResponse;
import com.sarthak.CommunicationService.model.Conversation;
import com.sarthak.CommunicationService.service.ConversationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService){
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<Conversation> createOrGet(@Valid @RequestBody CreateConversationRequest request){
        Conversation conversation = conversationService.getOrCreate(request.requesterId, request.senderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    @GetMapping("/{userId}")
    public List<ConversationResponse> getConversatonList(@PathVariable String userId){
        return conversationService.listOfUserConversations(userId);
    }

    public record CreateConversationRequest(@NotBlank String requesterId, @NotBlank String senderId){}
}
