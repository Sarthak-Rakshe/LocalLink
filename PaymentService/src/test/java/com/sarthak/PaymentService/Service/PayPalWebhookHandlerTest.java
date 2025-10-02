package com.sarthak.PaymentService.Service;

import com.sarthak.PaymentService.client.PayPalClient;
import com.sarthak.PaymentService.dto.response.WebhookResponse;
import com.sarthak.PaymentService.enums.PaymentMethod;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.exception.TransactionNotFoundException;
import com.sarthak.PaymentService.mapper.TransactionMapper;
import com.sarthak.PaymentService.model.Transaction;
import com.sarthak.PaymentService.repository.TransactionRepository;
import com.sarthak.PaymentService.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayPalWebhookHandlerTest {

    private static final String SAMPLE_WEBHOOK_PAYLOAD = "{\n" +
            "  \"id\": \"WH-58D329510W468432D-8HN650336L201105X\",\n" +
            "  \"create_time\": \"2019-02-14T21:50:07.940Z\",\n" +
            "  \"resource_type\": \"capture\",\n" +
            "  \"event_type\": \"PAYMENT.CAPTURE.COMPLETED\",\n" +
            "  \"summary\": \"Payment completed for $ 30.0 USD\",\n" +
            "  \"resource\": {\n" +
            "    \"supplementary_data\": {\n" +
            "      \"related_ids\": {\n" +
            "        \"order_id\": \"1AB234567A1234567\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"status\": \"COMPLETED\"\n" +
            "  }\n" +
            "}";

    private static final String ORDER_ID = "1AB234567A1234567";

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PayPalClient payPalClient;
    @Mock
    private TransactionMapper mapper; // mocked to avoid touching real mapping logic in these void method tests

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, payPalClient, mapper, null);
    }

    private Transaction buildTransaction(Long id, PaymentStatus status) {
        return Transaction.builder()
                .transactionId(id)
                .bookingId(100L)
                .customerId(200L)
                .amount(30.00)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(status)
                .transactionReference(ORDER_ID)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("handlePaypalWebhookEvent: success updates status to COMPLETED and saves transaction")
    void handlePaypalWebhookEvent_Success() {
        WebhookResponse webhookResponse = WebhookResponse.builder()
                .orderId(ORDER_ID)
                .paymentStatus(PaymentStatus.COMPLETED)
                .summary("Payment completed for $ 30.0 USD")
                .build();

        Transaction existing = buildTransaction(10L, PaymentStatus.PENDING);

        when(payPalClient.handleWebhookEvent(SAMPLE_WEBHOOK_PAYLOAD)).thenReturn(webhookResponse);
        when(transactionRepository.findByTransactionReference(ORDER_ID)).thenReturn(Optional.of(existing));
        // updateTransactionStatus will call findById again
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existing));
        // mapper is invoked inside updateTransactionStatus; stub it to avoid NPE on createdAt
        when(mapper.toDto(any(Transaction.class))).thenReturn(null);

        transactionService.handlePaypalWebhookEvent(SAMPLE_WEBHOOK_PAYLOAD);

        verify(payPalClient, times(1)).handleWebhookEvent(SAMPLE_WEBHOOK_PAYLOAD);
        verify(transactionRepository, times(1)).findByTransactionReference(ORDER_ID);
        verify(transactionRepository, times(1)).findById(10L);
        ArgumentCaptor<Transaction> savedCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(savedCaptor.capture());
        Transaction saved = savedCaptor.getValue();
        assertEquals(PaymentStatus.COMPLETED, saved.getPaymentStatus(), "Payment status should be updated to COMPLETED");
    }

    @Test
    @DisplayName("handlePaypalWebhookEvent: transaction not found throws exception")
    void handlePaypalWebhookEvent_TransactionNotFound() {
        WebhookResponse webhookResponse = WebhookResponse.builder()
                .orderId(ORDER_ID)
                .paymentStatus(PaymentStatus.COMPLETED)
                .summary("Payment completed for $ 30.0 USD")
                .build();

        when(payPalClient.handleWebhookEvent(SAMPLE_WEBHOOK_PAYLOAD)).thenReturn(webhookResponse);
        when(transactionRepository.findByTransactionReference(ORDER_ID)).thenReturn(Optional.empty());

        TransactionNotFoundException ex = assertThrows(TransactionNotFoundException.class,
                () -> transactionService.handlePaypalWebhookEvent(SAMPLE_WEBHOOK_PAYLOAD));
        assertTrue(ex.getMessage().contains(ORDER_ID));

        verify(transactionRepository, never()).findById(anyLong());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("handlePaypalWebhookEvent: no save when status already COMPLETED")
    void handlePaypalWebhookEvent_AlreadyCompleted_NoSave() {
        WebhookResponse webhookResponse = WebhookResponse.builder()
                .orderId(ORDER_ID)
                .paymentStatus(PaymentStatus.COMPLETED)
                .summary("Payment completed for $ 30.0 USD")
                .build();

        Transaction existing = buildTransaction(20L, PaymentStatus.COMPLETED);

        when(payPalClient.handleWebhookEvent(SAMPLE_WEBHOOK_PAYLOAD)).thenReturn(webhookResponse);
        when(transactionRepository.findByTransactionReference(ORDER_ID)).thenReturn(Optional.of(existing));
        when(transactionRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(mapper.toDto(existing)).thenReturn(null); // stub

        transactionService.handlePaypalWebhookEvent(SAMPLE_WEBHOOK_PAYLOAD);

        verify(transactionRepository, times(1)).findByTransactionReference(ORDER_ID);
        verify(transactionRepository, times(1)).findById(20L);
        // Because status already COMPLETED, updateTransactionStatus returns early -> no save
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("handlePaypalWebhookEvent: propagates PayPal client exception")
    void handlePaypalWebhookEvent_PayPalClientException() {
        RuntimeException simulated = new RuntimeException("PayPal error");
        when(payPalClient.handleWebhookEvent(SAMPLE_WEBHOOK_PAYLOAD)).thenThrow(simulated);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionService.handlePaypalWebhookEvent(SAMPLE_WEBHOOK_PAYLOAD));
        assertEquals("PayPal error", ex.getMessage());

        verify(transactionRepository, never()).findByTransactionReference(anyString());
        verify(transactionRepository, never()).save(any());
    }
}

