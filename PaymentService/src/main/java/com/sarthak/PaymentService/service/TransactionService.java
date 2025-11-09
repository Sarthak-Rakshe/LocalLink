package com.sarthak.PaymentService.service;

import com.sarthak.PaymentService.client.BookingClient;
import com.sarthak.PaymentService.client.PayPalClient;
import com.sarthak.PaymentService.config.shared.UserPrincipal;
import com.sarthak.PaymentService.dto.TransactionDto;
import com.sarthak.PaymentService.dto.request.CreateOrderRequest;
import com.sarthak.PaymentService.dto.request.TransactionFilter;
import com.sarthak.PaymentService.dto.request.PaymentRequest;
import com.sarthak.PaymentService.dto.response.CaptureOrderResponse;
import com.sarthak.PaymentService.dto.response.CreateOrderResponse;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Optional;

import static com.sarthak.PaymentService.enums.PaymentStatus.COMPLETED;
import static com.sarthak.PaymentService.enums.PaymentStatus.DECLINED;
import static com.sarthak.PaymentService.enums.PaymentStatus.PENDING;

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
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + transactionId));

        return mapper.toDto(transaction);
    }

    public CreateOrderResponse createOrder(
            CreateOrderRequest createOrderRequest) throws IOException, InterruptedException {

        LocalTime startTime = createOrderRequest.slot().startTime();
        LocalTime endTime = createOrderRequest.slot().endTime();
        BigDecimal amount = BigDecimal
                .valueOf(createOrderRequest.pricePerHour() * (endTime.getHour() - startTime.getHour()) / 88.0)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Creating PayPal order for amount: {} and requested payment method: {}", amount,
                createOrderRequest.paymentMethod());

        // validate payment method: ensure it's one of supported enum values
        String requestedMethod = createOrderRequest.paymentMethod();
        PaymentMethod pm = com.sarthak.PaymentService.enums.PaymentMethod
                .fromString(requestedMethod);
        if (pm == null) {
            log.error("Invalid payment method requested: {}", requestedMethod);
            throw new com.sarthak.PaymentService.exception.InvalidPaymentMethodException(
                    "Unsupported payment method: " + requestedMethod);
        }

        CreateOrderResponse response = payPalClient
                .createOrder(amount.toString(), requestedMethod);

        if (!response.status().equals("CREATED")) {
            log.error("Failed to create PayPal order. Status: {}", response.status());
            throw new FailedToCreatePaymentOrderException("PayPal return with status: " + response.status());
        }

        log.info("PayPal order created with ID: {} and allowedPaymentMethod: {}", response.orderId(),
                response.allowedPaymentMethod());

        return response;
    }

    public TransactionDto processPayment(PaymentRequest paymentRequest) {
        String orderId = paymentRequest.orderId();

        if (orderId == null || orderId.isEmpty()) {
            log.info("Order ID is null or empty in payment request");
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }

        PaymentStatus paymentStatus = PENDING;
        try {
            CaptureOrderResponse captureOrderResponse = payPalClient.captureOrder(orderId);
            log.info("Capture order response received for orderId: {} with status: {}", orderId,
                    captureOrderResponse.status());
            paymentStatus = PaymentStatus.fromString(captureOrderResponse.status());
            if (paymentStatus == null) {
                log.warn("Unknown PayPal status '{}' for order {} - treating as DECLINED", captureOrderResponse.status(), orderId);
                paymentStatus = DECLINED;
            }
        } catch (Exception e) {
            log.info("Exception occurred while capturing order for orderId: {}. Marking payment as DECLINED. Error: {}",
                    orderId, e.getMessage());
            paymentStatus = DECLINED;
        }

        Optional<Transaction> existingTransaction = transactionRepository.findByTransactionReference(orderId);
        if (existingTransaction.isPresent()) {
            Long transactionId = existingTransaction.get().getTransactionId();
            log.info("Transaction is already present for orderId: {}, updating status to {} if it is not completed",
                    orderId, paymentStatus);
            return updateTransactionStatus(transactionId, paymentStatus);
        }
        bookingClient.updateBookingStatus(paymentRequest.bookingId(), mapPaymentToBookingStatus(paymentStatus));
        return createTransaction(paymentRequest, paymentStatus);
    }

    @Transactional
    public TransactionDto createTransaction(PaymentRequest paymentRequest, PaymentStatus paymentStatus) {
        String orderId = paymentRequest.orderId();

        log.info("Creating transaction for orderId: {}", orderId);
        PaymentMethod paymentMethod = PaymentMethod.fromString(paymentRequest.paymentMethod());

        Transaction newTransaction = Transaction.builder()
                .bookingId(paymentRequest.bookingId())
                .customerId(paymentRequest.customerId())
                .serviceProviderId(paymentRequest.serviceProviderId())
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

    public Page<TransactionDto> getAllTransactions(int page, int size, String sortBy, String sortDirection,
            TransactionFilter filter, UserPrincipal userPrincipal) {

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

    @Transactional
    public TransactionDto updateTransactionStatus(Long transactionId, PaymentStatus updatedStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + transactionId));

        if (transaction.getPaymentStatus() == updatedStatus || transaction.getPaymentStatus() == COMPLETED) {
            log.info("No status update needed for transaction with id: {}. Current status: {}", transactionId,
                    transaction.getPaymentStatus());
            return mapper.toDto(transaction);
        }
        String bookingStatus = mapPaymentToBookingStatus(updatedStatus);

        bookingClient.updateBookingStatus(transaction.getBookingId(), bookingStatus);

        transaction.setPaymentStatus(updatedStatus);
        transactionRepository.save(transaction);

        return mapper.toDto(transaction);
    }

    public TransactionDto refreshPaymentStatus(String transactionReference) {
        if (transactionReference == null || transactionReference.isBlank()) {
            throw new TransactionReferenceNotValidException("Transaction reference must not be blank");
        }
        Optional<Transaction> optional = transactionRepository.findByTransactionReference(transactionReference);
        Transaction existing = optional.orElseThrow(() ->
                new TransactionNotFoundException("Transaction not found with reference: " + transactionReference));

        PaymentStatus updatedStatus = PENDING;
        try {
            CaptureOrderResponse response = payPalClient.captureOrder(transactionReference);
            updatedStatus = PaymentStatus.fromString(response.status());
            if (updatedStatus == null) {
                log.warn("Unknown PayPal status '{}' for reference {} - treating as DECLINED", response.status(), transactionReference);
                updatedStatus = DECLINED;
            }
        } catch (Exception e) {
            log.warn("Failed to capture order {} during refresh. Marking as DECLINED. Error: {}", transactionReference, e.getMessage());
            updatedStatus = DECLINED;
        }

        // Reuse existing logic which also updates booking status and persists
        return updateTransactionStatus(existing.getTransactionId(), updatedStatus);
    }

    private SortField validateSortField(String field) {
        return SortField.fromString(field);
    }

    private Pageable getPageable(int page, int size, String sortBy, String sortDirection) {
        if (page < 0)
            page = 0;
        if (size <= 0)
            size = 10;
        if (sortBy == null || sortBy.isEmpty())
            sortBy = "createdAt";
        if (sortDirection == null
                || (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc"))) {
            sortDirection = "desc";
        }
        SortField sortField = validateSortField(sortBy);

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortField.getFieldName()).descending()
                : Sort.by(sortField.getFieldName()).ascending();

        return PageRequest.of(page, size, sort);
    }

    private String mapPaymentToBookingStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case PENDING, DECLINED -> "PENDING";
            case FAILED -> "CANCELLED";
            case COMPLETED -> "CONFIRMED";
        };
    }
    /*
     * public void handlePaypalWebhookEvent(String payload){
     * WebhookResponse response = payPalClient.handleWebhookEvent(payload);
     * Transaction transaction =
     * transactionRepository.findByTransactionReference(response.orderId())
     * .orElseThrow(()-> new
     * TransactionNotFoundException("Transaction not found with reference: " +
     * response.orderId()));
     * updateTransactionStatus(transaction.getTransactionId(),
     * response.paymentStatus());
     * String bookingStatus;
     * if (Objects.requireNonNull(response.paymentStatus()) == COMPLETED) {
     * bookingStatus = "CONFIRMED";
     * } else if(response.paymentStatus() == DECLINED){
     * bookingStatus = "FAILED";
     * } else {
     * bookingStatus = "PENDING";
     * }
     * try{
     * BookingDto bookingDto =
     * bookingClient.updateBookingStatus(transaction.getBookingId(), bookingStatus);
     * }catch (Exception e){
     * log.error("Failed to update booking status for bookingId: {}. Error: {}",
     * transaction.getBookingId(),
     * e.getMessage());
     * }
     * }
     * 
     * public boolean verifyWebhookSignature(String payload, String signature,
     * String transmissionId, String transmissionTime, String certUrl, String
     * authAlgo){
     * return payPalClient.verifyWebhookSignature(payload, signature,
     * transmissionId, transmissionTime, certUrl, authAlgo);
     * }
     * 
     */
}
