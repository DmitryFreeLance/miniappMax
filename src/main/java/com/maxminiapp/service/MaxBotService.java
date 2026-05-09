package com.maxminiapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxminiapp.config.AppProperties;
import com.maxminiapp.integration.MaxBotClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class MaxBotService {

    private static final Logger log = LoggerFactory.getLogger(MaxBotService.class);

    private final AppProperties appProperties;
    private final MaxBotClient maxBotClient;
    private final UserService userService;

    public MaxBotService(AppProperties appProperties, MaxBotClient maxBotClient, UserService userService) {
        this.appProperties = appProperties;
        this.maxBotClient = maxBotClient;
        this.userService = userService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void configureWebhookOnStartup() {
        if (!maxBotClient.isConfigured()) {
            return;
        }

        String webhookUrl = appProperties.getPublicBaseUrl() + "/api/max/webhook";
        if (!webhookUrl.startsWith("https://")) {
            log.warn("MAX webhook should be HTTPS. Current URL: {}", webhookUrl);
            return;
        }

        try {
            maxBotClient.subscribeWebhook(webhookUrl, appProperties.getMax().getWebhookSecret());
            log.info("MAX webhook subscription request sent for {}", webhookUrl);
        } catch (Exception ex) {
            log.error("Failed to subscribe MAX webhook: {}", ex.getMessage(), ex);
        }
    }

    public void processUpdate(JsonNode update) {
        if (update == null) {
            return;
        }

        try {
            String type = extractUpdateType(update);
            Long userId = extractUserId(update);
            String text = extractText(update);

            log.info(
                    "MAX update received: type='{}', userId={}, text='{}'",
                    type,
                    userId,
                    text == null ? "" : text
            );

            if (userId == null) {
                return;
            }

            userService.getOrCreateByMaxUserId(userId);

            if ("bot_started".equals(type)) {
                sendWelcome(userId);
                return;
            }

            if ("message_created".equals(type)) {
                if (text == null) {
                    return;
                }

                String normalized = text.trim().toLowerCase();
                if (normalized.equals("/start")
                        || normalized.startsWith("/start@")
                        || normalized.equals("start")
                        || normalized.contains("начать")
                        || normalized.contains("каталог")
                        || normalized.contains("мини")) {
                    sendWelcome(userId);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to process MAX update: {}", ex.getMessage(), ex);
        }
    }

    private void sendWelcome(Long userId) {
        String miniAppUrl = appProperties.getMax().getMiniappUrl();
        String text = "Добро пожаловать! Нажмите «Открыть каталог», чтобы оформить заказ.";

        try {
            if (miniAppUrl == null || miniAppUrl.isBlank()) {
                maxBotClient.sendTextMessage(userId, text);
                return;
            }

            maxBotClient.sendMiniAppMessage(userId, text, miniAppUrl);
        } catch (Exception ex) {
            log.error("Failed to send mini app message, fallback to text: {}", ex.getMessage(), ex);
            try {
                maxBotClient.sendTextMessage(userId, text + " Если кнопка не отображается, откройте mini app из профиля бота.");
            } catch (Exception secondEx) {
                log.error("Fallback text message also failed: {}", secondEx.getMessage(), secondEx);
            }
        }
    }

    private String extractUpdateType(JsonNode update) {
        String primary = update.path("update_type").asText("");
        if (!primary.isBlank()) {
            return primary;
        }
        return update.path("type").asText("");
    }

    private Long extractUserId(JsonNode update) {
        long fromRootUser = update.path("user").path("user_id").asLong(0L);
        if (fromRootUser > 0) {
            return fromRootUser;
        }
        long fromRootUserIdAlias = update.path("user").path("id").asLong(0L);
        if (fromRootUserIdAlias > 0) {
            return fromRootUserIdAlias;
        }

        long fromMessageSender = update.path("message").path("sender").path("user_id").asLong(0L);
        if (fromMessageSender > 0) {
            return fromMessageSender;
        }
        long fromMessageSenderAlias = update.path("message").path("sender").path("id").asLong(0L);
        if (fromMessageSenderAlias > 0) {
            return fromMessageSenderAlias;
        }
        long fromCallbackSender = update.path("callback").path("user").path("user_id").asLong(0L);
        if (fromCallbackSender > 0) {
            return fromCallbackSender;
        }
        long fromCallbackSenderAlias = update.path("callback").path("user").path("id").asLong(0L);
        if (fromCallbackSenderAlias > 0) {
            return fromCallbackSenderAlias;
        }

        return null;
    }

    private String extractText(JsonNode update) {
        JsonNode body = update.path("message").path("body");
        if (!body.isMissingNode()) {
            String text = body.path("text").asText(null);
            if (text != null && !text.isBlank()) {
                return text;
            }

            String rawText = body.path("raw_text").asText(null);
            if (rawText != null && !rawText.isBlank()) {
                return rawText;
            }
        }

        String messageText = update.path("message").path("text").asText(null);
        if (messageText != null && !messageText.isBlank()) {
            return messageText;
        }

        String rootText = update.path("text").asText(null);
        if (rootText != null && !rootText.isBlank()) {
            return rootText;
        }

        return null;
    }
}
