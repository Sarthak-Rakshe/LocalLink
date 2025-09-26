package com.sarthak.PaymentService.service;

import com.sarthak.PaymentService.client.BookingClient;
import com.sarthak.PaymentService.client.PayPalClient;
import com.sarthak.PaymentService.dto.BookingDto;
import com.sarthak.PaymentService.dto.TransactionDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;


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

        PaymentStatus paymentStatus;

        log.info("Creating transaction for orderId: {}", orderId);

        Optional<Transaction> existingTransaction = transactionRepository.findByTransactionReference(orderId);

        if(existingTransaction.isPresent()){
            Long transactionId = existingTransaction.get().getTransactionId();
            String transactionReference = existingTransaction.get().getTransactionReference();
            log.info("Transaction is already present for orderId: {}, updating status to {} if it is not completed", orderId, paymentStatus);

            return updateTransactionStatus(transactionId, paymentStatus, transactionReference);

        }

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

        log.info("Confirming booking with id: {} via BookingClient", paymentRequest.bookingId());
        BookingDto confirmedBooking = bookingClient.confirmBooking(paymentRequest.bookingId());

        if(confirmedBooking == null){
            log.error("Failed to confirm booking with id: {} via BookingClient", paymentRequest.bookingId());
            throw  new BookingClientException("Booking client failed to confirm booking with id: " + paymentRequest.bookingId());
        }
        log.info("Booking with id: {} confirmed via BookingClient", paymentRequest.bookingId());

        return mapper.toDto(saved);
    }


    public Page<TransactionDto> getAllTransactions(int page, int size, String sortBy, String sortDirection){

        Pageable pageable = getPageable(page, size, sortBy, sortDirection);

        Page<Transaction> transactions = transactionRepository.findAll(pageable);

        log.info("Fetched {} transactions for page: {}, size: {}, sortBy: {}, sortDirection: {}",
                transactions.getNumberOfElements(), page, size, sortBy, sortDirection);

        return transactions.map(mapper::toDto);
    }

    public Page<TransactionDto> getTransactionsByBookingId(Long bookingId, int page, int size, String sortBy, String sortDirection){

        if(bookingId == null || bookingId <= 0){
            throw new IllegalArgumentException("Booking ID must be a positive number");
        }

        Pageable pageable = getPageable(page, size, sortBy, sortDirection);

        Page<Transaction> transactions = transactionRepository.findAllByBookingId(bookingId, pageable);

        log.info("Fetched {} transactions for bookingId: {} page: {}, size: {}, sortBy: {}, sortDirection: {}",
                transactions.getNumberOfElements(), bookingId, page, size, sortBy, sortDirection);

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

        Page<Transaction> transactions = transactionRepository.findAllByPaymentStatus(paymentStatus, pageable);

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

    public TransactionDto updateTransactionStatus(Long transactionId, PaymentStatus updatedStatus, String updatedTransactionReference){
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(()-> new TransactionNotFoundException("Transaction not found with id: " + transactionId));

        if (transaction.getPaymentStatus() == updatedStatus || transaction.getPaymentStatus() == PaymentStatus.COMPLETED) {
            log.info("No status update needed for transaction with id: {}. Current status: {}", transactionId, transaction.getPaymentStatus());
            return mapper.toDto(transaction);
        }

        Transaction updatedTransaction = Transaction.builder()
                .bookingId(transaction.getBookingId())
                .customerId(transaction.getCustomerId())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentStatus(updatedStatus)
                .transactionReference(updatedTransactionReference)
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

    }


}
