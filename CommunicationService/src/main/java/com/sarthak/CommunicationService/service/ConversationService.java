package com.sarthak.CommunicationService.service;

import com.sarthak.CommunicationService.dto.ConversationResponse;
import com.sarthak.CommunicationService.model.Conversation;
import com.sarthak.CommunicationService.model.Message;
import com.sarthak.CommunicationService.repository.ConversationRepository;
import com.sarthak.CommunicationService.repository.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    private String generatePairKey(String a, String b){
        return a.compareTo(b) <= 0 ? a + "_" + b : b + "_" + a;
    }

    @Transactional
    public Conversation getOrCreate(String requesterId, String otherUserId){
        if(requesterId.equals(otherUserId)) throw new IllegalArgumentException("Cannot start chat with self, yet.");
        String pairKey = generatePairKey(requesterId, otherUserId);
        return conversationRepository.findByPairKey(pairKey)
                .orElseGet(() -> conversationRepository.save(Conversation.builder().userAId(requesterId).userBId(otherUserId).build()));
    }

    public List<ConversationResponse> listOfUserConversations(String userId){
        return conversationRepository.findByUserAIdOrUserBIdOrderByIdDesc(userId, userId).stream().map( c -> {
            Optional<Message> last = messageRepository.findByConversationIdOrderByCreatedAtDesc(c.getId(), PageRequest.of(0,1)).stream().findFirst();
            String otherUser = c.getOtherParticipant(userId);
            return new ConversationResponse(
                    c.getId(),
                    otherUser,
                    last.map(Message::getContent).orElse(null),
                    last.map(Message::getCreatedAt).orElse(c.getCreatedAt())
            );
        }).toList();
    }

    public Conversation mustBelong(Long conversationId, String userId){
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
       if(!conversation.involvesUser(userId)) throw new IllegalArgumentException("Conversation does not belong to user");
       return conversation;
    }

    private static String snippet(String message){
        if(message.length() <= 40) return message;
        return message.substring(0, 37) + "...";
    }

}
