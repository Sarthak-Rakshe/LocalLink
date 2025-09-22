package com.sarthak.PaymentService.controller;

import com.sarthak.PaymentService.dto.TransactionDto;
import com.sarthak.PaymentService.dto.request.PaymentRequest;
import com.sarthak.PaymentService.dto.response.PagedResponse;
import com.sarthak.PaymentService.exception.FailedToCreatePaymentOrderException;
import com.sarthak.PaymentService.exception.PaymentProcessingException;
import com.sarthak.PaymentService.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final TransactionService transactionService;

    public PaymentController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(Long transactionId) {
        TransactionDto transactionDto = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transactionDto);
    }

    @GetMapping()
    public PagedResponse<TransactionDto> getAllTransactions(@NotNull @RequestParam(name ="sort-by") String sortBy,
                                                            @NotNull @RequestParam(name ="sort-dir") String sortDir,
                                                            @NotNull @RequestParam(name ="page") int page,
                                                            @NotNull @RequestParam(name ="size") int size) {

        Page<TransactionDto> transactions = transactionService.getAllTransactions(page, size, sortBy, sortDir);

        return new PagedResponse<>(
                transactions.getContent(),
                transactions.getNumber(),
                transactions.getSize(),
                transactions.getTotalElements(),
                transactions.getTotalPages()
        );
    }

    @GetMapping("bookings/{bookingId}")
    public PagedResponse<TransactionDto> getTransactionsByBookingId(@NotNull @PathVariable("bookingId") Long bookingId,
                                                                   @NotNull @RequestParam(name ="sort-by") String sortBy,
                                                                   @NotNull @RequestParam(name ="sort-dir") String sortDir,
                                                                   @NotNull @RequestParam(name ="page") int page,
                                                                   @NotNull @RequestParam(name ="size") int size) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByBookingId(bookingId, page, size, sortBy, sortDir);
        return new PagedResponse<>(
                transactions.getContent(),
                transactions.getNumber(),
                transactions.getSize(),
                transactions.getTotalElements(),
                transactions.getTotalPages()
        );
    }

    @GetMapping("/customer/{customerId}")
    public PagedResponse<TransactionDto> getTransactionsByCustomerId(@NotNull @PathVariable("customerId") Long customerId,
                                                                     @NotNull @RequestParam(name ="sort-by") String sortBy,
                                                                     @NotNull @RequestParam(name ="sort-dir") String sortDir,
                                                                     @NotNull @RequestParam(name ="page") int page,
                                                                     @NotNull @RequestParam(name ="size") int size) {

        Page<TransactionDto> transactions = transactionService.getTransactionsByCustomerId(customerId, page, size, sortBy, sortDir);

        return new PagedResponse<>(
                transactions.getContent(),
                transactions.getNumber(),
                transactions.getSize(),
                transactions.getTotalElements(),
                transactions.getTotalPages()
        );
    }

    @GetMapping("/payment/{paymentStatus}/status")
    public PagedResponse<TransactionDto> getTransactionsByPaymentStatus(@NotNull @PathVariable("paymentStatus") String paymentStatus,
                                                                       @NotNull @RequestParam(name ="sort-by") String sortBy,
                                                                       @NotNull @RequestParam(name ="sort-dir") String sortDir,
                                                                       @NotNull @RequestParam(name ="page") int page,
                                                                       @NotNull @RequestParam(name ="size") int size) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByPaymentStatus(paymentStatus, page, size, sortBy, sortDir);
        return new PagedResponse<>(
                transactions.getContent(),
                transactions.getNumber(),
                transactions.getSize(),
                transactions.getTotalElements(),
                transactions.getTotalPages()
        );
    }

    @GetMapping("/payment/{paymentMethod}/method")
    public PagedResponse<TransactionDto> getTransactionsByPaymentMethod(@NotNull @PathVariable("paymentMethod") String paymentMethod,
                                                                       @NotNull @RequestParam(name ="sort-by") String sortBy,
                                                                       @NotNull @RequestParam(name ="sort-dir") String sortDir,
                                                                       @NotNull @RequestParam(name ="page") int page,
                                                                       @NotNull @RequestParam(name ="size") int size) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByPaymentMethod(paymentMethod, page, size, sortBy, sortDir);
        return new PagedResponse<>(
                transactions.getContent(),
                transactions.getNumber(),
                transactions.getSize(),
                transactions.getTotalElements(),
                transactions.getTotalPages()
        );
    }

    @PostMapping("/createOrder/{amount}")
    public ResponseEntity<String> createOrder(@PathVariable("amount") Double amount) {
        String orderId = null;
        try {
            orderId = transactionService.createOrder(amount);
        } catch (IOException | InterruptedException e) {
            throw new FailedToCreatePaymentOrderException("Failed to create payment order: " + e.getMessage());
        }
        return ResponseEntity.ok(orderId);
    }

    @PostMapping("/processPayment")
    public ResponseEntity<TransactionDto> processPayment(@RequestBody @Valid PaymentRequest request){
        TransactionDto transactionDto = transactionService.processPayment(request);
        return ResponseEntity.ok(transactionDto);
    }

}
