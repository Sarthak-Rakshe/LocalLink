package com.sarthak.CommunicationService.service;

import com.sarthak.CommunicationService.dto.ChatMessageResponse;
import com.sarthak.CommunicationService.model.Message;
import com.sarthak.CommunicationService.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageBroadcastService messageBroadcastService;

    public MessageService(MessageRepository messageRepository, MessageBroadcastService messageBroadcastService){
        this.messageRepository = messageRepository;
        this.messageBroadcastService = messageBroadcastService;
    }

    public ChatMessageResponse persist(Long conversationId, String senderId, String content){
        String trimmed = content.trim();
        if(trimmed.isEmpty() || trimmed.length() > 2048){
            throw new IllegalArgumentException("Message content must be between 1 and 2048 characters");
        }
        trimmed = trimmed.replaceAll("\\p{Cntrl}", " ");

        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .content(trimmed)
                .build();

        messageRepository.save(message);
        ChatMessageResponse response = new ChatMessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getContent(),
                message.getCreatedAt()
        );
        // Broadcast over STOMP for real-time updates
        messageBroadcastService.broadcastToConversation(response);
        return response;
    }

    public Page<ChatMessageResponse> getPagedBefore(Long conversationId, int page, int size, Instant before){
        PageRequest pr = PageRequest.of(page, size);
        if (before != null){
            return messageRepository.findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(conversationId, before, pr)
                    .map(m -> new ChatMessageResponse(
                            m.getId(),
                            m.getConversationId(),
                            m.getSenderId(),
                            m.getContent(),
                            m.getCreatedAt()
                    ));
        }
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pr)
                .map(m -> new ChatMessageResponse(
                        m.getId(),
                        m.getConversationId(),
                        m.getSenderId(),
                        m.getContent(),
                        m.getCreatedAt()
                ));
    }

    public Page<ChatMessageResponse> getPagedAfter(Long conversationId, int page, int size, Instant after){
        PageRequest pr = PageRequest.of(page, size);
        if (after == null) throw new IllegalArgumentException("after parameter is required");
        return messageRepository.findByConversationIdAndCreatedAtAfterOrderByCreatedAtAsc(conversationId, after, pr)
                .map(m -> new ChatMessageResponse(
                        m.getId(),
                        m.getConversationId(),
                        m.getSenderId(),
                        m.getContent(),
                        m.getCreatedAt()
                ));
    }
}
