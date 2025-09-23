package com.sarthak.PaymentService.Service;

import com.sarthak.PaymentService.dto.TransactionDto;
import com.sarthak.PaymentService.dto.request.PaymentRequest;
import com.sarthak.PaymentService.dto.response.CreateOrderResponse;
import com.sarthak.PaymentService.enums.PaymentMethod;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.exception.FailedToCreatePaymentOrderException;
import com.sarthak.PaymentService.exception.TransactionNotFoundException;
import com.sarthak.PaymentService.mapper.TransactionMapper;
import com.sarthak.PaymentService.model.Transaction;
import com.sarthak.PaymentService.repository.TransactionRepository;
import com.sarthak.PaymentService.client.PayPalClient;
import com.sarthak.PaymentService.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PayPalClient payPalClient;

    @Mock
    private TransactionMapper mapper;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testGetTransactionById_found() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(mapper.toDto(transaction)).thenReturn(TransactionDto.builder().build());

        TransactionDto dto = transactionService.getTransactionById(1L);

        assertNotNull(dto);
        verify(transactionRepository).findById(1L);
        verify(mapper).toDto(transaction);
    }

    @Test
    void testGetTransactionById_notFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransactionById(1L));
    }

    @Test
    void testCreateOrder_success() throws IOException, InterruptedException {
        double amount = 100.0;
        CreateOrderResponse response = new CreateOrderResponse("order123", "CREATED");

        when(payPalClient.createOrder(String.valueOf(amount))).thenReturn(response);

        String orderId = transactionService.createOrder(amount);

        assertEquals("order123", orderId);
        verify(payPalClient).createOrder(String.valueOf(amount));
    }

    @Test
    void testCreateOrder_failure() throws IOException, InterruptedException {
        double amount = 100.0;
        CreateOrderResponse response = new CreateOrderResponse("order123", "FAILED");

        when(payPalClient.createOrder(String.valueOf(amount))).thenReturn(response);

        assertThrows(FailedToCreatePaymentOrderException.class, () -> transactionService.createOrder(amount));
    }

    @Test
    void testProcessPayment_newTransaction() throws IOException, InterruptedException {
        // PaymentRequest(orderId, bookingId, customerId, amount, paymentMethod)
        PaymentRequest request = new PaymentRequest("order123", 1L, 1L, 500.0, "CREDIT_CARD");

        when(payPalClient.captureOrder("order123")).thenReturn(PaymentStatus.COMPLETED);
        when(transactionRepository.findByTransactionReference("order123")).thenReturn(Optional.empty());

        Transaction savedTransaction = Transaction.builder()
                .transactionId(1L)
                .amount(500.0)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionReference("order123")
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(mapper.toDto(savedTransaction)).thenReturn(TransactionDto.builder().build());

        TransactionDto dto = transactionService.processPayment(request);

        assertNotNull(dto);
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals(PaymentStatus.COMPLETED, captor.getValue().getPaymentStatus());
    }

    @Test
    void testProcessPayment_existingTransaction() throws IOException, InterruptedException {
        PaymentRequest request = new PaymentRequest("order123", 1L, 1L, 500.0, "CREDIT_CARD");

        Transaction existingTransaction = Transaction.builder()
                .transactionId(1L)
                .transactionReference("order123")
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(payPalClient.captureOrder("order123")).thenReturn(PaymentStatus.COMPLETED);
        when(transactionRepository.findByTransactionReference("order123")).thenReturn(Optional.of(existingTransaction));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
        when(mapper.toDto(any(Transaction.class))).thenReturn(TransactionDto.builder().build());

        TransactionDto dto = transactionService.processPayment(request);

        assertNotNull(dto);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testGetAllTransactions() {
        Transaction transaction = new Transaction();
        Page<Transaction> page = new PageImpl<>(List.of(transaction));

        when(transactionRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(mapper.toDto(transaction)).thenReturn(TransactionDto.builder().build());

        Page<TransactionDto> result = transactionService.getAllTransactions(0, 10, null, null);

        assertEquals(1, result.getTotalElements());
    }
}
