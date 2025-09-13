package com.sarthak.CommunicationService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pair", columnNames = {"pair_key"})
})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_a_id", nullable = false)
    private String userAId;

    @Column(name = "user_b_id", nullable = false)
    private String userBId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "pair_key", nullable = false, unique = true, length = 512)
    private String pairKey;

    @PrePersist
    @PreUpdate
    private void generatePairKey(){
        String a = userAId.compareTo(userBId) <= 0 ? userAId : userBId;
        String b = userAId.compareTo(userBId) <= 0 ? userBId : userAId;
        this.pairKey = a + "_" + b;
    }

    public boolean involvesUser(String userId) {
        return userAId.equals(userId) || userBId.equals(userId);
    }

    public String getOtherParticipant(String userId) {
        return userAId.equals(userId) ? userBId : userAId;
    }
}
