package com.sarthak.PaymentService.controller;

import com.sarthak.PaymentService.config.shared.UserPrincipal;
import com.sarthak.PaymentService.dto.TransactionDto;
import com.sarthak.PaymentService.dto.request.TransactionFilter;
import com.sarthak.PaymentService.dto.request.PaymentRequest;
import com.sarthak.PaymentService.dto.response.PagedResponse;
import com.sarthak.PaymentService.exception.FailedToCreatePaymentOrderException;
import com.sarthak.PaymentService.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Slf4j
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
                                                            @NotNull @RequestParam(name ="size") int size,
                                                            Authentication authentication,
                                                            TransactionFilter transactionFilter) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Page<TransactionDto> transactions = transactionService.getAllTransactions(page, size, sortBy, sortDir,
                transactionFilter, userPrincipal);

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

    @PostMapping("/handleWebhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("PayPal-Transmission-Sig") String signature,
            @RequestHeader("PayPal-Transmission-Id") String transmissionId,
            @RequestHeader("PayPal-Transmission-Time") String transmissionTime,
            @RequestHeader("PayPal-Cert-Url") String certUrl,
            @RequestHeader("PayPal-Auth-Algo") String authAlgo
    ){
        log.info("Received webhook: {}", payload);

        boolean isValid = transactionService.verifyWebhookSignature(payload, signature, transmissionId,
                transmissionTime, certUrl, authAlgo);

        if (isValid){
            transactionService.handlePaypalWebhookEvent(payload);
            return ResponseEntity.ok("Webhook processed");
        } else {
            log.warn("Invalid webhook signature");
            return ResponseEntity.status(400).body("Invalid signature");
        }
    }

}
