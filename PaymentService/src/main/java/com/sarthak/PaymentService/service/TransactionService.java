package com.sarthak.PaymentService.service;

import com.sarthak.PaymentService.client.BookingClient;
import com.sarthak.PaymentService.client.PayPalClient;
import com.sarthak.PaymentService.config.shared.UserPrincipal;
import com.sarthak.PaymentService.dto.BookingDto;
import com.sarthak.PaymentService.dto.TransactionDto;
import com.sarthak.PaymentService.dto.request.TransactionFilter;
import com.sarthak.PaymentService.dto.request.PaymentRequest;
import com.sarthak.PaymentService.dto.response.CreateOrderResponse;
import com.sarthak.PaymentService.dto.response.WebhookResponse;
import com.sarthak.PaymentService.enums.SortField;
import com.sarthak.PaymentService.exception.*;
import com.sarthak.PaymentService.mapper.TransactionMapper;
import com.sarthak.PaymentService.enums.PaymentMethod;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.model.Transaction;
import com.sarthak.PaymentService.repository.TransactionRepository;
import com.sarthak.PaymentService.repository.specification.TransactionSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static com.sarthak.PaymentService.enums.PaymentStatus.COMPLETED;
import static javax.management.remote.JMXConnectionNotification.FAILED;


@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    public final PayPalClient payPalClient;
    private final BookingClient bookingClient;
    private final TransactionMapper mapper;

    public TransactionService(TransactionRepository transactionRepository, PayPalClient payPalClient,
                              TransactionMapper mapper, BookingClient bookingClient) {
        this.transactionRepository = transactionRepository;
        this.payPalClient = payPalClient;
        this.bookingClient = bookingClient;
        this.mapper = mapper;
    }

    public TransactionDto getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(()-> new TransactionNotFoundException("Transaction not found with id: " + transactionId));

        return mapper.toDto(transaction);
    }

    public String createOrder(Double amount) throws IOException, InterruptedException {

        log.info("Creating PayPal order for amount: {}", amount);
        CreateOrderResponse response =  payPalClient.createOrder(amount.toString());

        if(!response.status().equals("CREATED")){
            log.error("Failed to create PayPal order. Status: {}", response.status());
            throw new FailedToCreatePaymentOrderException("PayPal return with status: " + response.status());
        }

        log.info("PayPal order created with ID: {}", response.orderId());
        return response.orderId();
    }

    @Transactional
    public TransactionDto processPayment(PaymentRequest paymentRequest){
        String orderId = paymentRequest.orderId();

        PaymentStatus paymentStatus = PaymentStatus.PENDING;

        Optional<Transaction> existingTransaction = transactionRepository.findByTransactionReference(orderId);

        if(existingTransaction.isPresent()){
            Long transactionId = existingTransaction.get().getTransactionId();
            log.info("Transaction is already present for orderId: {}, updating status to {} if it is not completed", orderId, paymentStatus);
            return updateTransactionStatus(transactionId, paymentStatus);
        }

        log.info("Creating transaction for orderId: {}", orderId);
        PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentRequest.paymentMethod().toUpperCase());

        Transaction newTransaction = Transaction.builder()
                .bookingId(paymentRequest.bookingId())
                .customerId(paymentRequest.customerId())
                .amount(paymentRequest.amount())
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .transactionReference(orderId)
                .build();

        Transaction saved = transactionRepository.save(newTransaction);
        log.info("Transaction with id: {} and reference: {} processed with status: {}", saved.getTransactionId(),
                saved.getTransactionReference(), saved.getPaymentStatus());

        return mapper.toDto(saved);
    }

    public Page<TransactionDto> getAllTransactions(int page, int size, String sortBy, String sortDirection, TransactionFilter filter, UserPrincipal userPrincipal){

        Pageable pageable = getPageable(page, size, sortBy, sortDirection);
        Long userId = userPrincipal.getUserId();
        String userType = userPrincipal.getUserType();

        PaymentStatus statusFilter = PaymentStatus.fromString(filter.paymentStatus());
        PaymentMethod methodFilter = PaymentMethod.fromString(filter.paymentMethod());

        Specification<Transaction> spec = TransactionSpecification.resolveByUserType(userType, userId);
        if (methodFilter != null) {
            spec = TransactionSpecification.hasPaymentMethod(methodFilter);
        }
        if (statusFilter != null) {
            spec = spec.and(TransactionSpecification.hasPaymentStatus(statusFilter));
        }

        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);

        log.info("Fetched {} transactions for page: {}, size: {}, sortBy: {}, sortDirection: {}",
                transactions.getNumberOfElements(), page, size, sortBy, sortDirection);

        return transactions.map(mapper::toDto);
    }

    public TransactionDto updateTransactionStatus(Long transactionId, PaymentStatus updatedStatus){
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(()-> new TransactionNotFoundException("Transaction not found with id: " + transactionId));

        if (transaction.getPaymentStatus() == updatedStatus || transaction.getPaymentStatus() == COMPLETED) {
            log.info("No status update needed for transaction with id: {}. Current status: {}", transactionId, transaction.getPaymentStatus());
            return mapper.toDto(transaction);
        }

        Transaction updatedTransaction = Transaction.builder()
                .bookingId(transaction.getBookingId())
                .customerId(transaction.getCustomerId())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentStatus(updatedStatus)
                .transactionReference(transaction.getTransactionReference())
                .build();

        transactionRepository.save(updatedTransaction);

        return mapper.toDto(updatedTransaction);
    }

    private SortField validateSortField(String field) {
        return SortField.fromString(field);
    }

    private Pageable getPageable(int page, int size, String sortBy, String sortDirection) {
        if(page < 0) page = 0;
        if(size <= 0) size = 10;
        if(sortBy == null || sortBy.isEmpty()) sortBy = "createdAt";
        if(sortDirection == null || (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc"))) {
            sortDirection = "desc";
        }
        SortField sortField = validateSortField(sortBy);

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortField.getFieldName()).descending()
                : Sort.by(sortField.getFieldName()).ascending();

        return PageRequest.of(page, size, sort);
    }

    public void handlePaypalWebhookEvent(String payload){
        WebhookResponse response = payPalClient.handleWebhookEvent(payload);
        Transaction transaction = transactionRepository.findByTransactionReference(response.orderId())
                .orElseThrow(()-> new TransactionNotFoundException("Transaction not found with reference: " + response.orderId()));
        updateTransactionStatus(transaction.getTransactionId(), response.paymentStatus());
        String bookingStatus;
        if (Objects.requireNonNull(response.paymentStatus()) == COMPLETED) {
            bookingStatus = "CONFIRMED";
        } else {
            bookingStatus = "PENDING";
        }
        try{
            BookingDto bookingDto = bookingClient.updateBookingStatus(transaction.getBookingId(), bookingStatus);
        }catch (Exception e){
            log.error("Failed to update booking status for bookingId: {}. Error: {}", transaction.getBookingId(),
                    e.getMessage());
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature, String transmissionId, String transmissionTime, String certUrl, String authAlgo){
        return payPalClient.verifyWebhookSignature(payload, signature, transmissionId, transmissionTime, certUrl, authAlgo);
    }
}
