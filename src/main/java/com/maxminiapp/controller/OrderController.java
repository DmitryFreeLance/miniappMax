package com.maxminiapp.controller;

import com.maxminiapp.dto.CreateOrderRequest;
import com.maxminiapp.dto.CreateOrderResponse;
import com.maxminiapp.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public CreateOrderResponse createOrder(
            @RequestBody @Valid CreateOrderRequest request,
            @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        return orderService.createOrder(request, userId);
    }
}
