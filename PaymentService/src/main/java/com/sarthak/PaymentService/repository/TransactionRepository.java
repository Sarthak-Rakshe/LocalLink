package com.sarthak.PaymentService.repository;

import com.sarthak.PaymentService.enums.PaymentMethod;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    Page<Transaction> findAllByCustomerId(Long customerId, Pageable pageable);

    Page<Transaction> findAllByBookingId(Long bookingId, Pageable pageable);

    Page<Transaction> findAllByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    Page<Transaction> findAllByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    List<Transaction> findAllByPaymentStatusAndCreatedAtBefore(PaymentStatus paymentStatus, Instant cutOffTime);

    Page<Transaction> findAllByPaymentStatusAndPaymentMethod(PaymentStatus statusFilter, PaymentMethod methodFilter, Pageable pageable);
}
