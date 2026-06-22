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

    public void sendTextMessage(Long userId, String text) {
        if (!isConfigured()) {
            log.warn("MAX token is not configured, skip sending message");
            return;
        }

        Map<String, Object> payload = Map.of("text", text);
        sendMessage(userId, payload);
    }

    public void sendMiniAppMessage(Long userId, String text, String miniAppUrl) {
        if (!isConfigured()) {
            log.warn("MAX token is not configured, skip sending message");
            return;
        }

        String personalizedMiniAppUrl = buildMiniAppUrl(userId, miniAppUrl);
        Map<String, Object> payload = Map.of(
                "text", text + "\n" + personalizedMiniAppUrl,
                "attachments", List.of(
                        Map.of(
                                "type", "inline_keyboard",
                                "payload", Map.of(
                                        "buttons", List.of(
                                                List.of(
                                                        Map.of(
                                                                "type", "link",
                                                                "text", "Открыть каталог",
                                                                "url", personalizedMiniAppUrl
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        sendMessage(userId, payload);
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

    private void sendMessage(Long userId, Map<String, Object> payload) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/messages").queryParam("user_id", userId).build())
                .header(HttpHeaders.AUTHORIZATION, properties.getMax().getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private void sendLinkOnlyMessage(Long userId, String text, String miniAppUrl) {
        String personalizedMiniAppUrl = buildMiniAppUrl(userId, miniAppUrl);
        Map<String, Object> payload = Map.of(
                "text", text + "\n" + personalizedMiniAppUrl,
                "attachments", List.of(
                        Map.of(
                                "type", "inline_keyboard",
                                "payload", Map.of(
                                        "buttons", List.of(
                                                List.of(
                                                        Map.of(
                                                                "type", "link",
                                                                "text", "Открыть mini app",
                                                                "url", personalizedMiniAppUrl
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
        sendMessage(userId, payload);
    }

    private String buildMiniAppUrl(Long userId, String miniAppUrl) {
        if (miniAppUrl == null || miniAppUrl.isBlank() || userId == null) {
            return miniAppUrl;
        }

        int hashIndex = miniAppUrl.indexOf('#');
        String base = hashIndex >= 0 ? miniAppUrl.substring(0, hashIndex) : miniAppUrl;
        String hash = hashIndex >= 0 ? miniAppUrl.substring(hashIndex) : "";
        if (base.contains("userId=")) {
            return miniAppUrl;
        }
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "userId=" + userId + hash;
    }
}
