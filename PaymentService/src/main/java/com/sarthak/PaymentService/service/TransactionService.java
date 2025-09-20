package com.sarthak.PaymentService.service;

import com.sarthak.PaymentService.dto.TransactionDto;
import com.sarthak.PaymentService.dto.request.PaymentRequest;
import com.sarthak.PaymentService.dto.response.CreateOrderResponse;
import com.sarthak.PaymentService.enums.SortField;
import com.sarthak.PaymentService.exception.FailedToCreatePaymentOrderException;
import com.sarthak.PaymentService.exception.PaymentProcessingException;
import com.sarthak.PaymentService.exception.TransactionNotFoundException;
import com.sarthak.PaymentService.mapper.TransactionMapper;
import com.sarthak.PaymentService.enums.PaymentMethod;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.model.Transaction;
import com.sarthak.PaymentService.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PayPalClient payPalClient;
    private final TransactionMapper mapper;

    public TransactionService(TransactionRepository transactionRepository, PayPalClient payPalClient, TransactionMapper mapper) {
        this.transactionRepository = transactionRepository;
        this.payPalClient = payPalClient;
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

        PaymentStatus paymentStatus;

        log.info("Capturing order with id: {}", orderId);

        try{
            paymentStatus = payPalClient.captureOrder(orderId);
        }catch (IOException | InterruptedException exception){
            log.error("Error occurred while capturing order with id: {}", orderId, exception);
            throw new PaymentProcessingException("Error occurred while capturing order with id: " + orderId);
        }

        Transaction transaction = transactionRepository.findByTransactionReference(orderId)
                .orElseGet(() -> {
                            log.info("Creating new transaction for order id: {}", orderId);
                            return Transaction.builder()
                                    .bookingId(paymentRequest.bookingId())
                                    .customerId(paymentRequest.customerId())
                                    .amount(paymentRequest.amount())
                                    .paymentMethod(PaymentMethod.valueOf(paymentRequest.paymentMethod()))
                                    .paymentStatus(paymentStatus)
                                    .transactionReference(orderId)
                                    .build();
                        });

        if (transaction.getPaymentStatus() != PaymentStatus.COMPLETED) {
            log.info("Updating payment status for transaction with reference: {} from {} to {}", orderId, transaction.getPaymentStatus(),
                    paymentStatus);
            transaction.setPaymentStatus(paymentStatus);
        }else{
            log.info("Transaction with reference: {} is already completed. No update needed.", orderId);
        }

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction with id: {} and reference: {} processed with status: {}", saved.getTransactionId(),
                saved.getTransactionReference(), saved.getPaymentStatus());

        return mapper.toDto(saved);
    }


    public Page<TransactionDto> getAllTransactions(int page, int size, String sortBy, String sortDirection){

        Pageable pageable = getPageable(page, size, sortBy, sortDirection);

        Page<Transaction> transactions = transactionRepository.findAll(pageable);

        log.info("Fetched {} transactions for page: {}, size: {}, sortBy: {}, sortDirection: {}",
                transactions.getNumberOfElements(), page, size, sortBy, sortDirection);

        return transactions.map(mapper::toDto);
    }



    public Page<TransactionDto> getTransactionsByCustomerId(Long customerId, int page, int size, String sortBy, String sortDirection){

        if(customerId == null || customerId <= 0){
            throw new IllegalArgumentException("Customer ID must be a positive number");
        }

        Pageable pageable = getPageable(page, size, sortBy, sortDirection);

        Page<Transaction> transactions = transactionRepository.findAllByCustomerId(customerId, pageable);

        log.info("Fetched {} transactions for customerId: {} page: {}, size: {}, sortBy: {}, sortDirection: {}",
                transactions.getNumberOfElements(), customerId, page, size, sortBy, sortDirection);

        return transactions.map(mapper::toDto);
    }

    public Page<TransactionDto> getTransactionsByPaymentStatus(String status, int page, int size, String sortBy, String sortDirection){

        PaymentStatus paymentStatus;
        try{
            paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        }catch (IllegalArgumentException exception){
            log.warn("Invalid payment status: {} defaulting to COMPLETED", status);
            paymentStatus = PaymentStatus.COMPLETED;
        }

        Pageable pageable = getPageable(page, size, sortBy, sortDirection);

        Page<Transaction> transactions = transactionRepository.findAllByPaymentStatus(paymentStatus.name(), pageable);

        log.info("Fetched {} transactions for paymentStatus: {} page: {}, size: {}, sortBy: {}, sortDirection: {}",
                transactions.getNumberOfElements(), status, page, size, sortBy, sortDirection);

        return transactions.map(mapper::toDto);
    }

    public Page<TransactionDto> getTransactionsByPaymentMethod(String method, int page, int size, String sortBy, String sortDirection){

        PaymentMethod paymentMethod;
        try{
            paymentMethod = PaymentMethod.valueOf(method.toUpperCase());
        }catch (IllegalArgumentException exception){
            log.warn("Invalid payment method: {} defaulting to CASH", method);
            paymentMethod = PaymentMethod.CASH;
        }

        Pageable pageable = getPageable(page, size, sortBy, sortDirection);

        Page<Transaction> transactions = transactionRepository.findAllByPaymentMethod(paymentMethod.name(), pageable);

        log.info("Fetched {} transactions for paymentMethod: {} page: {}, size: {}, sortBy: {}, sortDirection: {}",
                transactions.getNumberOfElements(), method, page, size, sortBy, sortDirection);

        return transactions.map(mapper::toDto);
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

}
