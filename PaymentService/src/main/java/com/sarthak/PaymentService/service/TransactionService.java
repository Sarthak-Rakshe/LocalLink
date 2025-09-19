package com.sarthak.PaymentService.service;

import com.sarthak.PaymentService.dto.OrderCreateResponse;
import com.sarthak.PaymentService.dto.TransactionDto;
import com.sarthak.PaymentService.dto.request.PaymentRequest;
import com.sarthak.PaymentService.exception.FailedToCreatePaymentOrderException;
import com.sarthak.PaymentService.exception.PaymentProcessingException;
import com.sarthak.PaymentService.exception.TransactionNotFoundException;
import com.sarthak.PaymentService.mapper.TransactionMapper;
import com.sarthak.PaymentService.model.PaymentMethod;
import com.sarthak.PaymentService.model.PaymentStatus;
import com.sarthak.PaymentService.model.Transaction;
import com.sarthak.PaymentService.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
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

    public OrderCreateResponse createOrder(Double amount) throws IOException, InterruptedException {

        OrderCreateResponse response =  payPalClient.createOrder(amount.toString());

        if(!response.status().equals("CREATED")){
            throw new FailedToCreatePaymentOrderException("PayPal return with status: " + response.status());
        }

        return response;
    }

    @Transactional
    public TransactionDto processPayment(PaymentRequest paymentRequest){
        String orderId = paymentRequest.orderId();

        PaymentStatus paymentStatus;

        try{
            paymentStatus = payPalClient.captureOrder(orderId);
        }catch (IOException | InterruptedException exception){

            // log the exception (logging for the project is to be implemented)

            throw new PaymentProcessingException("Error occurred while capturing order with id: " + orderId);
        }

        Transaction transaction = transactionRepository.findByTransactionReference(orderId)
                .orElseGet( () -> Transaction.builder()
                        .bookingId(paymentRequest.bookingId())
                        .customerId(paymentRequest.customerId())
                        .amount(paymentRequest.amount())
                        .paymentMethod(PaymentMethod.valueOf(paymentRequest.paymentMethod()))
                        .paymentStatus(paymentStatus)
                        .transactionReference(orderId)
                        .build()
                );

        if (transaction.getPaymentStatus() != PaymentStatus.COMPLETED) {
            transaction.setPaymentStatus(paymentStatus);
        }
        return mapper.toDto(transactionRepository.save(transaction));
    }




}
