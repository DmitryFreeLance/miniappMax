package com.maxminiapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxminiapp.config.AppProperties;
import com.maxminiapp.exception.ForbiddenException;
import com.maxminiapp.service.MaxBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/max")
public class MaxWebhookController {

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
        String expectedSecret = appProperties.getMax().getWebhookSecret();
        if (expectedSecret != null && !expectedSecret.isBlank()) {
            if (secret == null || !expectedSecret.equals(secret)) {
                throw new ForbiddenException("Invalid MAX webhook secret");
            }
        }

        maxBotService.processUpdate(update);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
