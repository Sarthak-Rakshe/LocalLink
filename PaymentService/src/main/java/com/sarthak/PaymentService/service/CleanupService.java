package com.sarthak.PaymentService.service;

import com.sarthak.PaymentService.client.PayPalClient;
import com.sarthak.PaymentService.enums.PaymentMethod;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.model.Transaction;
import com.sarthak.PaymentService.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CleanupService {

    private final PayPalClient payPalClient;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final long UPDATE_INTERVAL_MS = 5 * 60 * 100; // 5 minutes
    private final long CLEANUP_INTERVAL_MS = 60 * 1000; // 1 minute

    public CleanupService(PayPalClient payPalClient, TransactionRepository transactionRepository, TransactionService transactionService) {
        this.payPalClient = payPalClient;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedRate = UPDATE_INTERVAL_MS)
    public void updatePendingTransactions(){
        log.debug("Scheduled task started: Checking pending transactions with PayPal");
        List<Transaction> pendingTransactions = transactionRepository
                .findAllByPaymentStatus(PaymentStatus.PENDING, Pageable.unpaged()).getContent()
                .stream()
                .filter(t -> t.getPaymentMethod() != PaymentMethod.CASH)
                .toList();

        int batchSize = 30;
        List<List<Transaction>> batches = partitionList(pendingTransactions, batchSize);
        for (List<Transaction> batch : batches) {
            log.debug("Processing batch of size: {}", batch.size());
            updateStatusBatchwise(batch);
            log.debug("Completed processing batch of size: {}", batch.size());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusBatchwise(List<Transaction> batch) {
        batch.forEach(t -> {
            try {
            PaymentStatus status = payPalClient.captureOrder(t.getTransactionReference());

            if (status == PaymentStatus.COMPLETED) {
                transactionService.updateTransactionStatus(t.getTransactionId(), PaymentStatus.COMPLETED, t.getTransactionReference());
                log.debug("Updated transaction with id: {} to COMPLETED", t.getTransactionId());
            } else if (status == PaymentStatus.DECLINED || status == PaymentStatus.FAILED) {
                transactionService.updateTransactionStatus(t.getTransactionId(), status, t.getTransactionReference());
                log.debug("Updated transaction with id: {} to {}", t.getTransactionId(), status);
            }
        } catch (Exception e) {
            log.error("Error checking status for transaction with id: {}. Error: {}", t.getTransactionId(), e.getMessage());
        }
        });

    }

    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    public void clearPendingTransactions(){
        log.debug("Scheduled task started: Clearing old pending transactions");

        Instant cutOffTime = Instant.now().minus(30, ChronoUnit.MINUTES);

        List<Transaction> oldPendingTransactions = transactionRepository
                .findAllByPaymentStatusAndCreatedAtBefore(PaymentStatus.PENDING, cutOffTime)
                .stream()
                        .filter( t-> t.getPaymentMethod() != PaymentMethod.CASH)
                        .toList();

        int batchSize = 30;
        List<List<Transaction>> batches = partitionList(oldPendingTransactions, batchSize);

        for (List<Transaction> batch : batches) {
            log.debug("Processing batch of size: {}", batch.size());
            clearPendingTransactionsBatchwise(batch);
            log.debug("Completed processing batch of size: {}", batch.size());
        }

        log.debug("Updated {} old pending transactions", oldPendingTransactions.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearPendingTransactionsBatchwise(List<Transaction> batch) {
        batch.forEach( t -> {
            try {
                transactionService.updateTransactionStatus(t.getTransactionId(), PaymentStatus.FAILED, t.getTransactionReference());
                log.debug("Updated old pending transaction with id: {} as FAILED", t.getTransactionId());
            } catch (Exception e) {
                log.error("Error marking old pending transaction with id: {} as FAILED. Error: {}", t.getTransactionId(), e.getMessage());
            }
        });
    }

    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();

        for(int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }

        return partitions;
    }


}
