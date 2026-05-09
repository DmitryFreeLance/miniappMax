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

        String type = update.path("update_type").asText("");
        Long userId = extractUserId(update);

        if (userId == null) {
            return;
        }

        userService.getOrCreateByMaxUserId(userId);

        if ("bot_started".equals(type)) {
            sendWelcome(userId);
            return;
        }

        if ("message_created".equals(type)) {
            String text = extractText(update);
            if (text == null) {
                return;
            }

            String normalized = text.trim().toLowerCase();
            if (normalized.equals("/start") || normalized.contains("каталог") || normalized.contains("мини")) {
                sendWelcome(userId);
            }
        }
    }

    private void sendWelcome(Long userId) {
        String url = appProperties.getMax().getMiniappUrl();
        String text = "Добро пожаловать! Откройте мини-приложение, чтобы выбрать стройматериалы и оформить заказ.";
        maxBotClient.sendMiniAppMessage(userId, text, url);
    }

    private Long extractUserId(JsonNode update) {
        long fromRootUser = update.path("user").path("user_id").asLong(0L);
        if (fromRootUser > 0) {
            return fromRootUser;
        }

        long fromMessageSender = update.path("message").path("sender").path("user_id").asLong(0L);
        if (fromMessageSender > 0) {
            return fromMessageSender;
        }

        return null;
    }

    private String extractText(JsonNode update) {
        JsonNode body = update.path("message").path("body");
        if (body.isMissingNode()) {
            return null;
        }
        String text = body.path("text").asText(null);
        if (text != null) {
            return text;
        }
        return update.path("text").asText(null);
    }
}
