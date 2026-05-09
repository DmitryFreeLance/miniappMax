package com.maxminiapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxminiapp.config.AppProperties;
import com.maxminiapp.exception.ForbiddenException;
import com.maxminiapp.service.MaxBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/max")
public class MaxWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MaxWebhookController.class);

    private final MaxBotService maxBotService;
    private final AppProperties appProperties;

    public MaxWebhookController(MaxBotService maxBotService, AppProperties appProperties) {
        this.maxBotService = maxBotService;
        this.appProperties = appProperties;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody JsonNode update,
            @RequestHeader(value = "X-Max-Bot-Api-Secret", required = false) String secret
    ) {
        String updateType = update == null ? "" : update.path("update_type").asText("");
        log.info(
                "MAX webhook request received: type='{}', secretHeaderPresent={}",
                updateType,
                secret != null && !secret.isBlank()
        );

        String expectedSecret = appProperties.getMax().getWebhookSecret();
        if (expectedSecret != null && !expectedSecret.isBlank()) {
            if (secret == null || !expectedSecret.equals(secret)) {
                log.warn("MAX webhook secret mismatch");
                throw new ForbiddenException("Invalid MAX webhook secret");
            }
        }

        try {
            maxBotService.processUpdate(update);
        } catch (Exception ex) {
            log.error("MAX webhook processing error: {}", ex.getMessage(), ex);
        }

        return ResponseEntity.ok(Map.of("ok", true));
    }
}
