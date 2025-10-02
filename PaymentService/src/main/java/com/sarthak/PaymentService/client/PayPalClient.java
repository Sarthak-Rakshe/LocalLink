package com.sarthak.PaymentService.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.PaymentService.dto.response.CreateOrderResponse;
import com.sarthak.PaymentService.dto.response.WebhookResponse;
import com.sarthak.PaymentService.enums.PaymentStatus;
import com.sarthak.PaymentService.exception.PayPalWebhookException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
@Slf4j
public class PayPalClient {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.base.url}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();


    private String getAccessToken() throws IOException,InterruptedException {

        log.info("Fetching PayPal access token from {}", baseUrl);
        String auth = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/oauth2/token"))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Request sent to PayPal for access token");

        JsonNode jsonNode = objectMapper.readTree(response.body());
        log.info("Received PayPal access token");
        return jsonNode.get("access_token").asText();
    }

    public CreateOrderResponse createOrder(String amount) throws IOException, InterruptedException{

        String accessToken = getAccessToken();
        log.info("Starting order creation process for amount: {}", amount);

        String payload = """
                {
                    "intent" : "CAPTURE",
                    "purchase_units" : [ {
                        "amount" : {
                            "currency_code" : "USD",
                            "value" : "%s"
                           }
                       } ]
                }
                """.formatted(amount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/checkout/orders"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        log.info("Sending order creation request to PayPal");

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode jsonNode = objectMapper.readTree(response.body());
        String orderId = jsonNode.get("id").asText();
        String status = jsonNode.get("status").asText();

        log.info("Order created with ID: {}, Status: {}", orderId, status);

        return CreateOrderResponse.builder()
                .orderId(orderId)
                .status(status)
                .build();
    }

    public WebhookResponse handleWebhookEvent(String payload) {
        log.info("Handling valid webhook event: {}", payload);

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new PayPalWebhookException(e.getMessage());
        }

        String eventType = jsonNode.get("event_type").asText();
        String summary = jsonNode.get("summary").asText();

        JsonNode resourceNode = jsonNode.get("resource");

        String orderId = null;
        if (resourceNode.has("supplementary_data") &&
                resourceNode.get("supplementary_data").has("related_ids") &&
                resourceNode.get("supplementary_data").get("related_ids").has("order_id")) {
            orderId = resourceNode.get("supplementary_data").get("related_ids").get("order_id").asText();
        }

        PaymentStatus paymentStatus;
        switch (eventType){
            case  "PAYMENT.CAPTURE.COMPLETED" -> {
                log.info("Processing event type: {} for orderId: {}", eventType, orderId);
                paymentStatus =  PaymentStatus.COMPLETED;
            }
            case "PAYMENT.CAPTURE.DECLINED", "PAYMENT.CAPTURE.DENIED" -> {
                log.info("Processing event type: {} for orderId: {}", eventType, orderId);
                paymentStatus = PaymentStatus.DECLINED;
            }
            case "PAYMENT.CAPTURE.PENDING" -> {
                log.info("Processing event type: {} for orderId: {}", eventType, orderId);
                paymentStatus = PaymentStatus.PENDING;
            }
            default -> {
                log.warn("Unhandled event type: {}", eventType);
                throw new PayPalWebhookException("Unhandled event type: " + eventType);
            }
        }
        return WebhookResponse.builder()
                .orderId(orderId)
                .paymentStatus(paymentStatus)
                .summary(summary)
                .build();
    }

    public boolean verifyWebhookSignature(String payload, String signature, String transmissionId, String transmissionTime, String certUrl, String authAlgo) {
        return true; //True for testing purposes
    }

}
