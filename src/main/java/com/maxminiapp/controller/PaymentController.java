package com.maxminiapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxminiapp.service.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/yookassa/webhook")
    public ResponseEntity<Map<String, Object>> yookassaWebhook(@RequestBody JsonNode payload) {
        paymentService.processYookassaWebhook(payload);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping(value = "/mock-success", produces = MediaType.TEXT_HTML_VALUE)
    public String mockSuccess(@RequestParam("paymentId") String paymentId) {
        paymentService.markPaid(paymentId);
        return """
                <html lang=\"ru\"><head><meta charset=\"UTF-8\"><title>Оплата успешна</title></head>
                <body style=\"font-family:Arial,sans-serif;padding:24px;\">
                <h2>Оплата подтверждена</h2>
                <p>Заказ отмечен как оплаченный. С вами свяжется менеджер.</p>
                </body></html>
                """;
    }
}
