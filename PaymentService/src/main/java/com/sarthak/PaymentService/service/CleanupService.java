package com.sarthak.PaymentService.service;

import com.sarthak.PaymentService.enums.PaymentMethod;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.model.Transaction;
import com.sarthak.PaymentService.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
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

    private final TransactionRepository transactionRepository;
    private final long CLEANUP_INTERVAL_MS = 300 * 1000; // 5 minutes

    public CleanupService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    public void clearPendingTransactions() {
        log.debug("Scheduled task started: Clearing old pending transactions");

        Instant cutOffTime = Instant.now().minus(30, ChronoUnit.MINUTES);

        List<Transaction> oldPendingTransactions = transactionRepository
                .findAllByPaymentStatusAndCreatedAtBefore(PaymentStatus.PENDING, cutOffTime)
                .stream()
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
        for (Transaction t : batch) {
            try {
                transactionRepository.findById(t.getTransactionId()).ifPresent(managed -> {
                    // double-check current state to avoid unexpected inserts/duplicates
                    if (managed.getPaymentStatus() != PaymentStatus.PENDING) return;

                    managed.setPaymentStatus(PaymentStatus.FAILED);
                    transactionRepository.save(managed); // will perform an update/merge for managed entity
                    log.debug("Updated old pending transaction with id: {} as FAILED", managed.getTransactionId());
                });
            } catch (Exception e) {
                log.error("Error marking old pending transaction with id: {} as FAILED. Error: {}", t.getTransactionId(), e.getMessage());
            }
        }
    }

    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }
}
