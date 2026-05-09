package com.maxminiapp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxminiapp.config.AppProperties;
import com.maxminiapp.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Component
public class YookassaClient {

    private static final Logger log = LoggerFactory.getLogger(YookassaClient.class);

    private final AppProperties properties;
    private final RestClient restClient;

    public YookassaClient(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.yookassa.ru/v3")
                .build();
    }

    public boolean isConfigured() {
        return notBlank(properties.getYookassa().getShopId()) && notBlank(properties.getYookassa().getSecretKey());
    }

    public PaymentCreationResult createPayment(BigDecimal amount, String description, Map<String, String> metadata) {
        if (!isConfigured()) {
            throw new BadRequestException("YooKassa не настроена: укажите YOOKASSA_SHOP_ID и YOOKASSA_SECRET_KEY");
        }

        String amountValue = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        Map<String, Object> payload = Map.of(
                "amount", Map.of(
                        "value", amountValue,
                        "currency", "RUB"
                ),
                "capture", true,
                "confirmation", Map.of(
                        "type", "redirect",
                        "return_url", properties.getYookassa().getReturnUrl()
                ),
                "description", description,
                "metadata", metadata
        );

        JsonNode response = restClient.post()
                .uri("/payments")
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
                .header("Idempotence-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new BadRequestException("Пустой ответ от YooKassa");
        }

        String paymentId = response.path("id").asText(null);
        String status = response.path("status").asText(null);
        String confirmationUrl = response.path("confirmation").path("confirmation_url").asText(null);

        if (paymentId == null) {
            log.error("YooKassa response without payment id: {}", response);
            throw new BadRequestException("Не удалось создать платеж в YooKassa");
        }

        return new PaymentCreationResult(paymentId, status, confirmationUrl);
    }

    private String basicAuthHeader() {
        String credentials = properties.getYookassa().getShopId() + ":" + properties.getYookassa().getSecretKey();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
