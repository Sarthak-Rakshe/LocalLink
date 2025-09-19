package com.sarthak.PaymentService.mapper;

import com.sarthak.PaymentService.dto.TransactionDto;
import com.sarthak.PaymentService.model.PaymentMethod;
import com.sarthak.PaymentService.model.PaymentStatus;
import com.sarthak.PaymentService.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.valueOf;

@Component
public class TransactionMapper {

    public TransactionDto toDto(Transaction transaction){
        return new TransactionDto(
                transaction.getTransactionId(),
                transaction.getBookingId(),
                transaction.getCustomerId(),
                transaction.getAmount(),
                transaction.getPaymentMethod().name(),
                transaction.getPaymentStatus().name(),
                transaction.getTransactionReference(),
                transaction.getCreatedAt().toString()
        );
    }

    public Transaction toEntity(TransactionDto transactionDto){
        return Transaction.builder()
                .transactionId(transactionDto.transactionId())
                .bookingId(transactionDto.bookingId())
                .customerId(transactionDto.customerId())
                .amount(transactionDto.amount())
                .paymentMethod(PaymentMethod.valueOf(transactionDto.paymentMethod()))
                .paymentStatus(PaymentStatus.valueOf(transactionDto.paymentStatus()))
                .transactionReference(transactionDto.transactionReference())
                .build();
    }

    public List<TransactionDto> toDtoList(List<Transaction> transactions){
        return transactions.stream().map(this::toDto).toList();
    }

}
