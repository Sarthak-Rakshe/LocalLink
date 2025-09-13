package com.sarthak.CommunicationService.repository;

import com.sarthak.CommunicationService.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByPairKey(String pairKey);
    List<Conversation> findByUserAIdOrUserBIdOrderByIdDesc(String userAId, String userBId);
}
