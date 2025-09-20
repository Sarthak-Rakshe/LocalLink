package com.sarthak.PaymentService.repository;

import com.sarthak.PaymentService.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String transactionReference);


    Page<Transaction> findAllByCustomerId(Long customerId, Pageable pageable);

    Page<Transaction> findAllByBookingId(Long bookingId, Pageable pageable);

    Page<Transaction> findAllByPaymentStatus(String paymentStatus, Pageable pageable);

    Page<Transaction> findAllByPaymentMethod(String name, Pageable pageable);
}
