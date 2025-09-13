package com.sarthak.CommunicationService.service;

import com.sarthak.CommunicationService.dto.ChatMessageResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageBroadcastService {
    private final SimpMessagingTemplate messagingTemplate;

    public MessageBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastToConversation(ChatMessageResponse message){
        String destination = "/topic/conversations/" + message.conversationId();
        messagingTemplate.convertAndSend(destination, message);
    }
}

