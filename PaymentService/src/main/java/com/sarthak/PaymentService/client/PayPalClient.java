package com.sarthak.PaymentService.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.PaymentService.dto.response.CaptureOrderResponse;
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

    private void ensure2xx(HttpResponse<String> response, String action) {
        int status = response.statusCode();
        if (status / 100 != 2) {
            log.error("PayPal {} failed. Status: {}, Body: {}", action, status, response.body());
            throw new IllegalStateException("PayPal " + action + " failed with status " + status);
        }
    }

    private String readRequired(JsonNode node, String field, String action) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            throw new IllegalStateException("PayPal " + action + " missing field: " + field);
        }
        String value = valueNode.asText();
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("PayPal " + action + " empty field: " + field);
        }
        return value;
    }

    private String getAccessToken() throws IOException, InterruptedException {
        log.info("Fetching PayPal access token from {}", baseUrl);
        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/oauth2/token"))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ensure2xx(response, "token");
        log.debug("PayPal token response: {}", response.body());

        JsonNode jsonNode = objectMapper.readTree(response.body());
        String accessToken = readRequired(jsonNode, "access_token", "token");
        log.info("Received PayPal access token");
        return accessToken;
    }

    public CreateOrderResponse createOrder(String amount) throws IOException, InterruptedException {
        String accessToken = getAccessToken();
        log.info("Starting order creation process for amount: {}", amount);

        String payload = """
                {
                  "intent": "CAPTURE",
                  "purchase_units": [
                    {
                      "amount": {
                        "currency_code": "USD",
                        "value": "%s"
                      }
                    }
                  ]
                }
                """.formatted(amount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/checkout/orders"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ensure2xx(response, "create order");
        log.debug("PayPal create order response: {}", response.body());

        JsonNode jsonNode = objectMapper.readTree(response.body());
        String orderId = readRequired(jsonNode, "id", "create order");
        String status = readRequired(jsonNode, "status", "create order");

        log.info("Order created with ID: {}, Status: {}", orderId, status);
        return CreateOrderResponse.builder().orderId(orderId).status(status).build();
    }

    public CaptureOrderResponse captureOrder(String orderId) throws IOException, InterruptedException {
        String accessToken = getAccessToken();
        log.info("Starting order capture process for orderId: {}", orderId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/checkout/orders/" + orderId + "/capture"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ensure2xx(response, "capture order");
        log.debug("PayPal capture order response: {}", response.body());

        JsonNode jsonNode = objectMapper.readTree(response.body());
        String status = readRequired(jsonNode, "status", "capture order");

        log.info("Order captured with ID: {}, Status: {}", orderId, status);
        return CaptureOrderResponse.builder().orderId(orderId).status(status).build();
    }
/*
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
*/
}
