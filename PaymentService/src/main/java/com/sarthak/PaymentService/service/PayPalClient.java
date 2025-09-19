package com.sarthak.PaymentService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.PaymentService.dto.OrderCreateResponse;
import com.sarthak.PaymentService.model.OrderStatus;
import com.sarthak.PaymentService.model.PaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
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

        JsonNode jsonNode = objectMapper.readTree(response.body());
        return jsonNode.get("access_token").asText();
    }

    public OrderCreateResponse createOrder(String amount) throws IOException, InterruptedException{
        String accessToken = getAccessToken();

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

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode jsonNode = objectMapper.readTree(response.body());
        String orderId = jsonNode.get("id").asText();
        String status = jsonNode.get("status").asText();
        String link = jsonNode.get("links").get(1).get("href").asText(); // Approve link


        return OrderCreateResponse.builder()
                .orderId(orderId)
                .status(status)
                .link(link)
                .build();
    }

    public PaymentStatus captureOrder(String orderId) throws IOException, InterruptedException{
        String accessToken = getAccessToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/checkout/orders/" + orderId + "/capture"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode jsonNode = objectMapper.readTree(response.body());

        String status = jsonNode.get("status").asText();

        return PaymentStatus.valueOf(status);
    }

}
