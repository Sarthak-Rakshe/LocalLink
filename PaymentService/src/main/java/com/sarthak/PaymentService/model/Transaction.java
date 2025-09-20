package com.sarthak.PaymentService.model;

import com.sarthak.PaymentService.enums.PaymentMethod;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.exception.TransactionReferenceNotValidException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactions", indexes = {
        @Index(name = "idx_booking_id", columnList = "booking_id"),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_payment_status", columnList = "payment_status"),
        @Index(name = "idx_payment_method", columnList = "payment_method"),
        @Index(name = "idx_transaction_reference", columnList = "transaction_reference"),
        @Index(name = "idx_customer_payment_status", columnList = "customer_id, payment_status")
        },
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_transaction_reference", columnNames = {"transaction_reference"})
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @NotNull
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotNull
    @PositiveOrZero(message = "Amount must be zero or positive")
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @NotNull
    @Column(name = "transaction_reference", unique = true)
    private String transactionReference; // Reference ID from payment gateway

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        }
        if(transactionReference == null || transactionReference.isEmpty() && !(paymentMethod.equals(PaymentMethod.CASH))) {
            throw new TransactionReferenceNotValidException("Transaction reference cannot be null or empty for non-cash payments");
        }
        if(paymentMethod.equals(PaymentMethod.CASH)) {
            this.paymentStatus = PaymentStatus.PENDING;
            this.transactionReference = "CASH-" + bookingId + "-" + Instant.now().toEpochMilli();
        }

    }

}
