package com.maxminiapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxminiapp.config.AppProperties;
import com.maxminiapp.enums.OrderStatus;
import com.maxminiapp.enums.QuantityUnit;
import com.maxminiapp.integration.PaymentCreationResult;
import com.maxminiapp.integration.YookassaClient;
import com.maxminiapp.model.Order;
import com.maxminiapp.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final AppProperties appProperties;
    private final OrderRepository orderRepository;
    private final YookassaClient yookassaClient;
    private final ProductService productService;

    public PaymentService(
            AppProperties appProperties,
            OrderRepository orderRepository,
            YookassaClient yookassaClient,
            ProductService productService
    ) {
        this.appProperties = appProperties;
        this.orderRepository = orderRepository;
        this.yookassaClient = yookassaClient;
        this.productService = productService;
    }

    @Transactional
    public PaymentInitResult initPayment(Order order) {
        if (appProperties.getPayment().isMockEnabled()) {
            String paymentId = "mock-" + UUID.randomUUID();
            String paymentUrl = appProperties.getPublicBaseUrl() + "/api/payments/mock-success?paymentId=" + paymentId;

            order.setPaymentId(paymentId);
            order.setPaymentUrl(paymentUrl);
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            orderRepository.save(order);

            return new PaymentInitResult(paymentId, paymentUrl, OrderStatus.PENDING_PAYMENT,
                    "Включен mock-режим: используйте ссылку для имитации успешной оплаты");
        }

        PaymentCreationResult payment = yookassaClient.createPayment(
                order.getTotalPrice(),
                "Заказ №" + order.getId(),
                Map.of("order_id", String.valueOf(order.getId()))
        );

        order.setPaymentId(payment.id());
        order.setPaymentUrl(payment.confirmationUrl());

        if ("succeeded".equals(payment.status())) {
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
        } else if ("canceled".equals(payment.status())) {
            order.setStatus(OrderStatus.CANCELED);
            productService.restoreStock(order.getProduct(), order.getQuantityUnit(), order.getQuantity());
        } else {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
        }

        orderRepository.save(order);

        return new PaymentInitResult(
                payment.id(),
                payment.confirmationUrl(),
                order.getStatus(),
                "Платеж создан"
        );
    }

    @Transactional
    public void processYookassaWebhook(JsonNode payload) {
        if (payload == null) {
            return;
        }

        String event = payload.path("event").asText("");
        JsonNode object = payload.path("object");
        String paymentId = object.path("id").asText(null);
        String status = object.path("status").asText("");

        if (paymentId == null || paymentId.isBlank()) {
            return;
        }

        if ("payment.succeeded".equals(event) || "succeeded".equals(status)) {
            markPaid(paymentId);
            return;
        }

        if ("payment.canceled".equals(event) || "canceled".equals(status)) {
            markCanceled(paymentId);
        }
    }

    @Transactional
    public void markPaid(String paymentId) {
        Optional<Order> optionalOrder = orderRepository.findByPaymentId(paymentId);
        if (optionalOrder.isEmpty()) {
            log.warn("Payment {} does not match any order", paymentId);
            return;
        }

        Order order = optionalOrder.get();
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    public void markCanceled(String paymentId) {
        Optional<Order> optionalOrder = orderRepository.findByPaymentId(paymentId);
        if (optionalOrder.isEmpty()) {
            log.warn("Canceled payment {} does not match any order", paymentId);
            return;
        }

        Order order = optionalOrder.get();
        if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.FAILED) {
            return;
        }

        order.setStatus(OrderStatus.CANCELED);
        productService.restoreStock(order.getProduct(), order.getQuantityUnit(), order.getQuantity());
        orderRepository.save(order);
    }
}
