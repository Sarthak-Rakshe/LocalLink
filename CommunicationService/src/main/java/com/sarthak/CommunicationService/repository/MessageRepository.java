package com.sarthak.CommunicationService.repository;

import com.sarthak.CommunicationService.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
    Page<Message> findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(Long conversationId, Instant before,
                                                                             Pageable pageable);
    Page<Message> findByConversationIdAndCreatedAtAfterOrderByCreatedAtAsc(Long conversationId, Instant after,
                                                                           Pageable pageable);
}
