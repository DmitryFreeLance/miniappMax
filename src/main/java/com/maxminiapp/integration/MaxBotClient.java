package com.maxminiapp.integration;

import com.maxminiapp.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class MaxBotClient {

    private static final Logger log = LoggerFactory.getLogger(MaxBotClient.class);

    private final AppProperties properties;
    private final RestClient restClient;

    public MaxBotClient(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl("https://platform-api.max.ru")
                .build();
    }

    public boolean isConfigured() {
        return properties.getMax().getToken() != null && !properties.getMax().getToken().isBlank();
    }

    public void sendMiniAppMessage(Long userId, String text, String miniAppUrl) {
        if (!isConfigured()) {
            log.warn("MAX token is not configured, skip sending message");
            return;
        }

        Map<String, Object> payload = Map.of(
                "text", text,
                "attachments", List.of(
                        Map.of(
                                "type", "inline_keyboard",
                                "payload", Map.of(
                                        "buttons", List.of(
                                                List.of(
                                                        Map.of(
                                                                "type", "link",
                                                                "text", "Открыть каталог",
                                                                "url", miniAppUrl
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/messages").queryParam("user_id", userId).build())
                .header(HttpHeaders.AUTHORIZATION, properties.getMax().getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    public void subscribeWebhook(String webhookUrl, String secret) {
        if (!isConfigured()) {
            log.warn("MAX token is not configured, skip webhook subscription");
            return;
        }

        Map<String, Object> payload;
        if (secret != null && !secret.isBlank()) {
            payload = Map.of(
                    "url", webhookUrl,
                    "update_types", List.of("message_created", "bot_started"),
                    "secret", secret
            );
        } else {
            payload = Map.of(
                    "url", webhookUrl,
                    "update_types", List.of("message_created", "bot_started")
            );
        }

        restClient.post()
                .uri("/subscriptions")
                .header(HttpHeaders.AUTHORIZATION, properties.getMax().getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
