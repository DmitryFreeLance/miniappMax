package com.maxminiapp.controller;

import com.maxminiapp.config.AppProperties;
import com.maxminiapp.dto.CreateOrderRequest;
import com.maxminiapp.dto.CreateOrderResponse;
import com.maxminiapp.dto.OrderResponse;
import com.maxminiapp.dto.PaymentDetailsResponse;
import com.maxminiapp.service.AppSettingsService;
import com.maxminiapp.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final AppSettingsService appSettingsService;
    private final AppProperties appProperties;

    public OrderController(
            OrderService orderService,
            AppSettingsService appSettingsService,
            AppProperties appProperties
    ) {
        this.orderService = orderService;
        this.appSettingsService = appSettingsService;
        this.appProperties = appProperties;
    }

    @PostMapping
    public CreateOrderResponse createOrder(
            @RequestBody @Valid CreateOrderRequest request,
            @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        return orderService.createOrder(request, userId);
    }

    @GetMapping("/payment-details")
    public PaymentDetailsResponse paymentDetails() {
        return new PaymentDetailsResponse(appSettingsService.getPaymentDetails(), appProperties.getCityDeliveryFee());
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders(
            @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        return orderService.getOrdersForUser(userId);
    }
}
