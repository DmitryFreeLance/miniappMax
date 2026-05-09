package com.maxminiapp.service;

import com.maxminiapp.config.AppProperties;
import com.maxminiapp.dto.CreateOrderRequest;
import com.maxminiapp.dto.CreateOrderResponse;
import com.maxminiapp.dto.OrderResponse;
import com.maxminiapp.enums.DeliveryMethod;
import com.maxminiapp.enums.OrderStatus;
import com.maxminiapp.enums.PaymentMethod;
import com.maxminiapp.exception.BadRequestException;
import com.maxminiapp.model.AppUser;
import com.maxminiapp.model.Order;
import com.maxminiapp.model.Product;
import com.maxminiapp.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final AppProperties appProperties;
    private final AppSettingsService appSettingsService;
    private final OrderNotificationService orderNotificationService;

    public OrderService(
            OrderRepository orderRepository,
            UserService userService,
            ProductService productService,
            AppProperties appProperties,
            AppSettingsService appSettingsService,
            OrderNotificationService orderNotificationService
    ) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
        this.appProperties = appProperties;
        this.appSettingsService = appSettingsService;
        this.orderNotificationService = orderNotificationService;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, Long headerUserId) {
        Long maxUserId = headerUserId != null ? headerUserId : request.getUserId();
        if (maxUserId == null) {
            throw new BadRequestException("Нужен ID пользователя MAX (X-User-Id или userId в запросе)");
        }

        BigDecimal quantity = request.getQuantity().setScale(3, RoundingMode.HALF_UP);
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Количество должно быть больше 0");
        }

        String fullName = request.getFullName().trim();
        String phone = request.getPhone().trim();
        String address = request.getAddress() == null ? "" : request.getAddress().trim();

        if (fullName.isBlank() || phone.isBlank()) {
            throw new BadRequestException("Укажите ФИО и телефон");
        }

        if (request.getDeliveryMethod() == DeliveryMethod.CITY_DELIVERY && address.isBlank()) {
            throw new BadRequestException("Укажите адрес доставки");
        }

        if (request.getDeliveryMethod() == DeliveryMethod.PICKUP && address.isBlank()) {
            address = "Самовывоз";
        }

        if (request.getDeliveryMethod() == DeliveryMethod.OTHER && address.isBlank()) {
            address = "Другая доставка (обсуждается)";
        }

        PaymentMethod paymentMethod = request.getPaymentMethod();
        String paymentDetails = appSettingsService.getPaymentDetails().trim();
        if (paymentMethod == PaymentMethod.CARD_NOW && paymentDetails.isBlank()) {
            throw new BadRequestException("Реквизиты для оплаты пока не настроены администратором");
        }

        AppUser user = userService.getOrCreateByMaxUserId(maxUserId);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);

        Product product = productService.getByIdOrThrow(request.getProductId());
        if (!product.isActive()) {
            throw new BadRequestException("Товар недоступен для заказа");
        }

        BigDecimal unitPrice = productService.resolveUnitPrice(product, request.getQuantityUnit());
        BigDecimal itemsTotal = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deliveryFee = resolveDeliveryFee(request.getDeliveryMethod());
        BigDecimal total = itemsTotal.add(deliveryFee).setScale(2, RoundingMode.HALF_UP);

        productService.reserveStock(product, request.getQuantityUnit(), quantity);

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setQuantityUnit(request.getQuantityUnit());
        order.setUnitPrice(unitPrice);
        order.setItemsTotal(itemsTotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalPrice(total);
        order.setFullName(fullName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setDeliveryMethod(request.getDeliveryMethod());
        order.setPaymentMethod(paymentMethod);

        if (paymentMethod == PaymentMethod.CARD_NOW) {
            order.setStatus(OrderStatus.PAID);
            order.setPaymentDetailsSnapshot(paymentDetails);
            order.setPaidAt(LocalDateTime.now());
        } else {
            order.setStatus(OrderStatus.CREATED);
            order.setPaymentDetailsSnapshot(null);
        }

        order = orderRepository.save(order);

        try {
            orderNotificationService.notifyAdminsAboutNewOrder(order);
        } catch (Exception ex) {
            log.warn("Failed to notify admins about order {}: {}", order.getId(), ex.getMessage());
        }

        return new CreateOrderResponse(
                order.getId(),
                order.getItemsTotal(),
                order.getDeliveryFee(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getPaymentMethod(),
                paymentMethod == PaymentMethod.CARD_NOW
                        ? "Платеж отмечен как выполненный. С вами свяжется менеджер."
                        : "Заказ создан с оплатой при получении. С вами свяжется менеджер."
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getMaxUserId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getQuantityUnit(),
                order.getUnitPrice(),
                order.getItemsTotal(),
                order.getDeliveryFee(),
                order.getTotalPrice(),
                order.getDeliveryMethod(),
                order.getPaymentMethod(),
                order.getFullName(),
                order.getPhone(),
                order.getAddress(),
                order.getStatus(),
                order.getPaymentDetailsSnapshot(),
                order.getCreatedAt(),
                order.getPaidAt()
        );
    }

    private BigDecimal resolveDeliveryFee(DeliveryMethod deliveryMethod) {
        if (deliveryMethod == DeliveryMethod.CITY_DELIVERY) {
            BigDecimal fee = appProperties.getCityDeliveryFee();
            if (fee == null) {
                return new BigDecimal("1000.00");
            }
            return fee.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
