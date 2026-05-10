package com.maxminiapp.service;

import com.maxminiapp.integration.MaxBotClient;
import com.maxminiapp.model.AppUser;
import com.maxminiapp.model.Order;
import com.maxminiapp.model.OrderItem;
import com.maxminiapp.repository.OrderItemRepository;
import com.maxminiapp.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class OrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationService.class);

    private final UserService userService;
    private final MaxBotClient maxBotClient;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderNotificationService(
            UserService userService,
            MaxBotClient maxBotClient,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.userService = userService;
        this.maxBotClient = maxBotClient;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public void notifyAdminsAboutNewOrder(Long orderId) {
        if (!maxBotClient.isConfigured() || orderId == null) {
            return;
        }

        Optional<Order> maybeOrder = orderRepository.findById(orderId);
        if (maybeOrder.isEmpty()) {
            return;
        }

        Order order = maybeOrder.get();
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(orderId);
        String text = buildOrderCreatedMessage(order, items);

        List<AppUser> admins = userService.findAdmins();
        for (AppUser admin : admins) {
            Long maxUserId = admin.getMaxUserId();
            if (maxUserId == null) {
                continue;
            }

            try {
                maxBotClient.sendTextMessage(maxUserId, text);
            } catch (Exception ex) {
                log.warn("Failed to notify admin {}: {}", maxUserId, ex.getMessage());
            }
        }
    }

    public void notifyCustomerOrderAccepted(Long customerMaxUserId, String eta) {
        if (!maxBotClient.isConfigured() || customerMaxUserId == null) {
            return;
        }

        String text = "Заказ принят.\nОриентировочное время доставки: " + eta;
        maxBotClient.sendTextMessage(customerMaxUserId, text);
    }

    private String buildOrderCreatedMessage(Order order, List<OrderItem> items) {
        StringBuilder builder = new StringBuilder()
                .append("Новый заказ №").append(order.getId()).append("\n")
                .append("Позиций: ").append(items.isEmpty() ? 1 : items.size()).append("\n");

        if (!items.isEmpty()) {
            for (OrderItem item : items) {
                builder.append("• ")
                        .append(item.getProductName())
                        .append(" — ")
                        .append(item.getQuantity().stripTrailingZeros().toPlainString())
                        .append(" ")
                        .append(unitLabel(item.getQuantityUnit() == null ? null : item.getQuantityUnit().name()))
                        .append(" × ")
                        .append(item.getUnitPrice().setScale(2, RoundingMode.HALF_UP).toPlainString())
                        .append(" ₽ = ")
                        .append(item.getLineTotal().setScale(2, RoundingMode.HALF_UP).toPlainString())
                        .append(" ₽\n");
            }
        } else if (order.getProduct() != null) {
            builder.append("• ")
                    .append(order.getProduct().getName())
                    .append(" — ")
                    .append(order.getQuantity().stripTrailingZeros().toPlainString())
                    .append(" ")
                    .append(unitLabel(order.getQuantityUnit() == null ? null : order.getQuantityUnit().name()))
                    .append("\n");
        }

        builder.append("Сумма: ").append(order.getTotalPrice()).append(" ₽\n")
                .append("Оплата: ").append(paymentMethodLabel(order)).append("\n")
                .append("Доставка: ").append(deliveryMethodLabel(order)).append("\n")
                .append("Клиент: ").append(order.getFullName()).append(", ").append(order.getPhone());

        return builder.toString();
    }

    private String unitLabel(String unit) {
        if (unit == null) {
            return "-";
        }
        return "PCS".equals(unit) ? "шт" : "куб.м";
    }

    private String paymentMethodLabel(Order order) {
        if (order.getPaymentMethod() == null) {
            return "-";
        }
        return switch (order.getPaymentMethod()) {
            case CARD_NOW -> "Сейчас (карта)";
            case ON_DELIVERY -> "При получении";
        };
    }

    private String deliveryMethodLabel(Order order) {
        if (order.getDeliveryMethod() == null) {
            return "-";
        }
        return switch (order.getDeliveryMethod()) {
            case CITY_DELIVERY -> "Доставка по городу";
            case PICKUP -> "Самовывоз";
            case OTHER -> "Другая доставка";
        };
    }
}
