package com.maxminiapp.service;

import com.maxminiapp.dto.CreateOrderRequest;
import com.maxminiapp.dto.CreateOrderResponse;
import com.maxminiapp.dto.OrderResponse;
import com.maxminiapp.enums.OrderStatus;
import com.maxminiapp.exception.BadRequestException;
import com.maxminiapp.model.AppUser;
import com.maxminiapp.model.Order;
import com.maxminiapp.model.Product;
import com.maxminiapp.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final PaymentService paymentService;

    public OrderService(
            OrderRepository orderRepository,
            UserService userService,
            ProductService productService,
            PaymentService paymentService
    ) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
        this.paymentService = paymentService;
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

        AppUser user = userService.getOrCreateByMaxUserId(maxUserId);
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress().trim());

        Product product = productService.getByIdOrThrow(request.getProductId());
        if (!product.isActive()) {
            throw new BadRequestException("Товар недоступен для заказа");
        }

        BigDecimal unitPrice = product.getPrice();
        BigDecimal total = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        productService.reserveStock(product, request.getQuantityUnit(), quantity);

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setQuantityUnit(request.getQuantityUnit());
        order.setUnitPrice(unitPrice);
        order.setTotalPrice(total);
        order.setFullName(request.getFullName().trim());
        order.setPhone(request.getPhone().trim());
        order.setAddress(request.getAddress().trim());
        order.setStatus(OrderStatus.CREATED);

        order = orderRepository.save(order);

        PaymentInitResult payment = paymentService.initPayment(order);

        return new CreateOrderResponse(
                order.getId(),
                order.getTotalPrice(),
                payment.status(),
                payment.paymentId(),
                payment.paymentUrl(),
                payment.status() == OrderStatus.PAID
                        ? "Оплата подтверждена. С вами свяжется менеджер."
                        : "Заказ создан. Перейдите к оплате по ссылке."
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
                order.getTotalPrice(),
                order.getFullName(),
                order.getPhone(),
                order.getAddress(),
                order.getStatus(),
                order.getPaymentId(),
                order.getPaymentUrl(),
                order.getCreatedAt(),
                order.getPaidAt()
        );
    }
}
