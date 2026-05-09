package com.maxminiapp.service;

import com.maxminiapp.config.AppProperties;
import com.maxminiapp.integration.MaxBotClient;
import com.maxminiapp.model.AppUser;
import com.maxminiapp.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationService.class);

    private final AppProperties appProperties;
    private final UserService userService;
    private final MaxBotClient maxBotClient;

    public OrderNotificationService(
            AppProperties appProperties,
            UserService userService,
            MaxBotClient maxBotClient
    ) {
        this.appProperties = appProperties;
        this.userService = userService;
        this.maxBotClient = maxBotClient;
    }

    public void notifyAdminsAboutNewOrder(Order order) {
        if (!maxBotClient.isConfigured()) {
            return;
        }

        String text = buildMessage(order);
        List<AppUser> admins = userService.findAdmins();
        for (AppUser admin : admins) {
            Long maxUserId = admin.getMaxUserId();
            if (maxUserId == null) {
                continue;
            }

            try {
                String miniAppUrl = appProperties.getMax().getMiniappUrl();
                if (miniAppUrl != null && !miniAppUrl.isBlank()) {
                    maxBotClient.sendMiniAppMessage(maxUserId, text, miniAppUrl);
                } else {
                    maxBotClient.sendTextMessage(maxUserId, text);
                }
            } catch (Exception ex) {
                log.warn("Failed to notify admin {}: {}", maxUserId, ex.getMessage());
            }
        }
    }

    private String buildMessage(Order order) {
        return "Новый заказ №" + order.getId() + "\n"
                + "Товар: " + order.getProduct().getName() + "\n"
                + "Сумма: " + order.getTotalPrice() + " ₽\n"
                + "Оплата: " + paymentMethodLabel(order) + "\n"
                + "Доставка: " + deliveryMethodLabel(order) + "\n"
                + "Клиент: " + order.getFullName() + ", " + order.getPhone();
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
